package com.project.transactionapi.controller;

import com.project.shared.dto.PayRequest;
import com.project.shared.dto.PayResponse;
import com.project.transactionapi.model.Transaction;
import com.project.transactionapi.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService service;
    private final Logger log = LoggerFactory.getLogger(TransactionController.class);

    public TransactionController(TransactionService service) { this.service = service; }

    @PostMapping("/pay")
    public ResponseEntity<PayResponse> pay(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                           @Valid @RequestBody PayRequest req) {
        Transaction txn = service.createAndProcess(idempotencyKey, req.getAmount(), req.getCurrency(), "STUB");
        return new ResponseEntity<>(new PayResponse(txn.getTxnId(), txn.getStatus(), txn.getCreatedAt()), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{txnId}")
    public ResponseEntity<Transaction> get(@PathVariable String txnId) {
        return service.findByTxnId(txnId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
