package com.project.transactionapi.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="transactions")
public class Transaction {
    @Id
    private UUID id;

    @Column(name="txn_id", nullable=false, unique=true)
    private String txnId;

    @Column(nullable=false)
    private Long amount;

    @Column(nullable=false)
    private String currency;

    @Column(nullable=false)
    private String status; // PENDING, SUCCESS, FAILED, RETRYING, FAILED_PERMANENTLY

    private Integer retryCount = 0;

    private Integer maxRetries = 3;

    @Column(name="next_retry_at")
    private OffsetDateTime nextRetryAt;

    private String provider;

    @Column(name="provider_txn_id")
    private String providerTxnId;

    @Column(columnDefinition="jsonb")
    private String metadata;

    @Column(name="created_at")
    private OffsetDateTime createdAt;

    @Column(name="updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate
    public void preUpdate() { updatedAt = OffsetDateTime.now(); }

    // getters & setters omitted for brevity (generate via IDE)
    // ...
}
