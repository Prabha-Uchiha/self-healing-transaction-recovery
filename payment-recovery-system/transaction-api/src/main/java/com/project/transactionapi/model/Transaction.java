package com.project.transactionapi.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private UUID id;

    private long amount;
    private String currency;
    private String status; // PENDING, SUCCESS, FAILED, RETRYING, FAILED_PERMANENTLY
    private int retryCount = 0;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String idempotencyKey;
    private String provider;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { updatedAt = OffsetDateTime.now(); }

    // getters & setters omitted for brevity
}