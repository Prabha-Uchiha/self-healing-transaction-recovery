package com.project.retry.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RecoveryService {
    private final NamedParameterJdbcTemplate jdbc;
    private static final Logger log = LoggerFactory.getLogger(RecoveryService.class);

    public RecoveryService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Poll a small batch of rows and process them. Uses FOR UPDATE SKIP LOCKED to avoid contention.
     */
    @Transactional
    public void pollAndProcessRetries(int limit) {
        String sql = """
            SELECT id, txn_id, retry_count, max_retries
            FROM transactions
            WHERE status IN ('FAILED','RETRYING')
              AND (next_retry_at IS NULL OR next_retry_at <= :now)
            ORDER BY next_retry_at NULLS FIRST, created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """;

        Map<String,Object> params = new HashMap<>();
        params.put("now", OffsetDateTime.now());
        params.put("limit", limit);

        List<Map<String,Object>> rows = jdbc.queryForList(sql, params);

        for (var row : rows) {
            UUID id = (UUID)row.get("id");
            String txnId = (String)row.get("txn_id");
            Integer retryCount = (Integer)row.get("retry_count");
            Integer maxRetries = (Integer)row.get("max_retries");
            log.info("Processing retry for txn {}", txnId);

            try {
                // Simulate processing by calling the transaction-api or provider.
                // For MVP we reattempt via an HTTP call to transaction-api endpoint.
                boolean success = attemptRemoteProcessing(txnId);
                if (success) {
                    jdbc.update("UPDATE transactions SET status='SUCCESS', updated_at = now() WHERE id = :id",
                            Collections.singletonMap("id", id));
                    log.info("Txn {} recovered -> SUCCESS", txnId);
                } else {
                    retryCount = (retryCount == null ? 1 : retryCount + 1);
                    if (retryCount >= (maxRetries == null ? 3 : maxRetries)) {
                        jdbc.update("UPDATE transactions SET status='FAILED_PERMANENTLY', retry_count=:rc, updated_at=now() WHERE id=:id",
                                Map.of("rc", retryCount, "id", id));
                        log.warn("Txn {} reached max retries -> FAILED_PERMANENTLY", txnId);
                    } else {
                        // set exponential backoff
                        long backoff = (long)Math.pow(2, Math.min(retryCount, 6));
                        OffsetDateTime next = OffsetDateTime.now().plusSeconds(backoff);
                        jdbc.update("UPDATE transactions SET retry_count=:rc, next_retry_at=:next, status='RETRYING', updated_at=now() WHERE id=:id",
                                Map.of("rc", retryCount, "next", next, "id", id));
                        log.info("Scheduled next retry for txn {} at {}", txnId, next);
                    }
                }
            } catch (Exception ex) {
                log.error("Unexpected error while retrying txn {}: {}", txnId, ex.getMessage(), ex);
            }
        }
    }

    // MVP remote attempt - for production you'd integrate via queue or service call
    private boolean attemptRemoteProcessing(String txnId) {
        // For MVP: call transaction-api /api/v1/transactions/retry/{txnId} OR call provider directly.
        // Here simply simulate with random success 50%
        return Math.random() > 0.5;
    }
}
