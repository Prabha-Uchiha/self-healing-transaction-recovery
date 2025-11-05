package com.project.transactionapi.service;

import com.project.transactionapi.gateway.PaymentGateway;
import com.project.transactionapi.model.Transaction;
import com.project.transactionapi.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repo;
    private final IdempotencyService idempotency;
    private final PaymentGateway gateway;

    public TransactionService(TransactionRepository repo,
                              IdempotencyService idempotency,
                              PaymentGateway gateway) {
        this.repo = repo;
        this.idempotency = idempotency;
        this.gateway = gateway;
    }

    /**
     * Creates and processes a transaction (synchronously for MVP).
     * Uses idempotency key to avoid duplicates.
     */
    @Transactional
    public Transaction createAndProcess(String idempotencyKey, long amount, String currency, String provider) {

        // Handle idempotency check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String existingTxnId = idempotency.get(idempotencyKey);
            if (existingTxnId != null) {
                Optional<Transaction> existing = repo.findByTxnId(existingTxnId);
                if (existing.isPresent()) {
                    log.info("Returning existing transaction for idempotencyKey={}", idempotencyKey);
                    return existing.get();
                }
            }
        }

        // Create new transaction
        Transaction txn = new Transaction();
        txn.setTxnId(UUID.randomUUID().toString());
        txn.setAmount(amount);
        txn.setCurrency(currency);
        txn.setStatus("PENDING");
        txn.setProvider(provider);
        txn.setRetryCount(0);
        txn.setNextRetryAt(null);

        txn = repo.save(txn);

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotency.claim(idempotencyKey, txn.getTxnId(), Duration.ofHours(24));
        }

        // For MVP, process synchronously (production → async / queue)
        processOnce(txn);
        return txn;
    }

    /**
     * Attempts to process a transaction once.
     * Updates retry count and schedules next attempt if failed.
     */
    @Transactional
    public Transaction processOnce(Transaction txn) {
        try {
            var resp = gateway.charge(txn);

            if (resp.success) {
                txn.setStatus("SUCCESS");
                txn.setProviderTxnId(resp.providerTxnId);
                txn.setNextRetryAt(null);
                log.info("Transaction [{}] succeeded via provider={}", txn.getTxnId(), txn.getProvider());
            } else {
                txn.setStatus("FAILED");

                // ✅ Fixed: remove null comparison, increment safely
                int attempts = txn.getRetryCount() + 1;
                txn.setRetryCount(attempts);

                // Schedule retry with exponential backoff (2^n seconds, max 64s)
                long backoffSeconds = (long) Math.pow(2, Math.min(attempts, 6));
                txn.setNextRetryAt(OffsetDateTime.now().plusSeconds(backoffSeconds));

                log.warn("Transaction [{}] failed (attempt #{}) — next retry at {}",
                        txn.getTxnId(), attempts, txn.getNextRetryAt());
            }

            return repo.save(txn);

        } catch (Exception e) {
            log.error("Error processing transaction [{}]: {}", txn.getTxnId(), e.getMessage(), e);
            txn.setStatus("ERROR");
            return repo.save(txn);
        }
    }

    /**
     * Finds a transaction by its ID.
     */
    public Optional<Transaction> findByTxnId(String txnId) {
        return repo.findByTxnId(txnId);
    }
}
