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

@Service
public class TransactionService {
    private final TransactionRepository repo;
    private final IdempotencyService idempotency;
    private final PaymentGateway gateway;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepository repo, IdempotencyService idempotency, PaymentGateway gateway) {
        this.repo = repo; this.idempotency = idempotency; this.gateway = gateway;
    }

    @Transactional
    public Transaction createAndProcess(String idempotencyKey, long amount, String currency, String provider) {
        String idClaim = null;
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idClaim = idempotency.get(idempotencyKey);
            if (idClaim != null) {
                Optional<Transaction> existing = repo.findByTxnId(idClaim);
                if (existing.isPresent()) return existing.get();
            }
        }
        Transaction txn = new Transaction();
        txn.setTxnId(java.util.UUID.randomUUID().toString());
        txn.setAmount(amount);
        txn.setCurrency(currency);
        txn.setStatus("PENDING");
        txn.setProvider(provider);
        txn = repo.save(txn);
        if (idempotencyKey != null) {
            idempotency.claim(idempotencyKey, txn.getTxnId(), Duration.ofHours(24));
        }

        // call provider sync for MVP — production should enqueue
        processOnce(txn);
        return txn;
    }

    @Transactional
    public Transaction processOnce(Transaction txn) {
        var resp = gateway.charge(txn);
        if (resp.success) {
            txn.setStatus("SUCCESS");
            txn.setProviderTxnId(resp.providerTxnId);
            txn.setNextRetryAt(null);
        } else {
            txn.setStatus("FAILED");
            txn.setRetryCount(txn.getRetryCount() == null ? 1 : txn.getRetryCount() + 1);
            // schedule next retry with exponential backoff
            int attempts = txn.getRetryCount();
            long backoffSeconds = (long) Math.pow(2, Math.min(attempts, 6));
            txn.setNextRetryAt(OffsetDateTime.now().plusSeconds(backoffSeconds));
        }
        return repo.save(txn);
    }

    public Optional<Transaction> findByTxnId(String txnId) {
        return repo.findByTxnId(txnId);
    }
}
