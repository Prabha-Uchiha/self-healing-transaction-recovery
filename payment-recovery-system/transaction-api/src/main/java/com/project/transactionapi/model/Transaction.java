package com.project.transactionapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String txnId;

    private Double amount;
    private String status;           // PENDING, SUCCESS, FAILED
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters, Setters, Constructors
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
