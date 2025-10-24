package com.project.transactionapi.gateway;

import com.project.transactionapi.model.Transaction;

public interface PaymentGateway {
    GatewayResponse charge(Transaction txn);
    class GatewayResponse {
        public final boolean success;
        public final String providerTxnId;
        public final String message;
        public GatewayResponse(boolean success, String providerTxnId, String message) {
            this.success = success; this.providerTxnId = providerTxnId; this.message = message;
        }
    }
}
