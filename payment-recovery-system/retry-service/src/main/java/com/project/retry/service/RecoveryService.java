package com.project.retry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecoveryService {

    @Transactional
    public void processPendingRetries() {
        // query transactions with status PENDING/RETRYING
        // simulate provider call (random success/failure)
        // update status and retryCount
    }
}