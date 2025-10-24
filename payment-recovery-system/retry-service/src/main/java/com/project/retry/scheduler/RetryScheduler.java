package com.project.retry.scheduler;

import com.project.retry.service.RecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);
    private final RecoveryService recovery;

    public RetryScheduler(RecoveryService recovery) { this.recovery = recovery; }

    // run every 15s in MVP; tune in prod
    @Scheduled(fixedDelayString = "${retry.fixedDelayMs:15000}")
    public void run() {
        log.debug("RetryScheduler tick");
        recovery.pollAndProcessRetries(10);
    }
}
