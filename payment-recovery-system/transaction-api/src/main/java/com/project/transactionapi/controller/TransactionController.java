package com.project.transactionapi.controller;

import com.project.transactionapi.model.Transaction;
import com.project.transactionapi.repository.TransactionRepository;
import com.project.shared.dto.PayRequest;
import com.project.shared.dto.PayResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired
    private TransactionRepository repo;

    @PostMapping("/pay")
    public ResponseEntity<PayResponse> pay(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                           @RequestBody PayRequest req) {
        // Idempotency via DB lookup
        if (idempotencyKey != null) {
            Optional<Transaction> existing = repo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                Transaction t = existing.get();
                return ResponseEntity.ok(new PayResponse(t.getId().toString(), t.getStatus()));
            }
        }

        Transaction txn = new Transaction();
        txn.setAmount(req.getAmount());
        txn.setCurrency(req.getCurrency());
        txn.setStatus("PENDING");
        txn.setProvider(req.getProvider());
        txn.setIdempotencyKey(idempotencyKey);
        repo.save(txn);

        // call provider stub async in later days — for Day1 we simulate success/fail in retry service
        return ResponseEntity.ok(new PayResponse(txn.getId().toString(), txn.getStatus()));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> get(@PathVariable String id) {
        return repo.findById(java.util.UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}