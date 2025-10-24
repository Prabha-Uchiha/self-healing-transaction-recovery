package com.project.transactionapi.gateway;

import com.project.transactionapi.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StubPaymentGateway implements PaymentGateway {
    @Override
    public GatewayResponse charge(Transaction txn) {
        // deterministic-ish stub: 70% success
        boolean success = Math.random() > 0.3;
        String provId = "STUB-" + UUID.randomUUID();
        return new GatewayResponse(success, success ? provId : null, success ? "OK" : "TEMP_FAILURE");
    }
}
