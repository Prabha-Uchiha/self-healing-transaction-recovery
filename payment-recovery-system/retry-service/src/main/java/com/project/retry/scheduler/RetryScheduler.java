package com.project.retry.scheduler;

import com.project.retry.service.RecoveryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class RetryScheduler {

    @Autowired
    private RecoveryService recoveryService;

    @Scheduled(fixedDelay = 10000)
    public void pollFailedTransactions() {
        recoveryService.processPendingRetries();
    }
}