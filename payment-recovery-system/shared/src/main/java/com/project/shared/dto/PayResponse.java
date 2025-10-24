package com.project.shared.dto;

import java.time.OffsetDateTime;

public class PayResponse {
    private String txnId;
    private String status;
    private OffsetDateTime createdAt;

    public PayResponse(String txnId, String status, OffsetDateTime createdAt) {
        this.txnId = txnId;
        this.status = status;
        this.createdAt = createdAt;
    }
    // getters/setters
    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(OffsetDateTime t){this.createdAt = t;}
}
