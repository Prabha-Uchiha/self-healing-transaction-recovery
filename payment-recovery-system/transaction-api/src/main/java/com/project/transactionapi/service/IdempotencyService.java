package com.project.transactionapi.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {
    private final RedisTemplate<String, String> redis;

    public IdempotencyService(RedisTemplate<String, String> redis) { this.redis = redis; }

    /**
     * Try to claim idempotency key.
     * @return true if claimed (caller should proceed); false if someone claimed before.
     */
    public boolean claim(String key, String txnId, Duration ttl) {
        // set if absent
        Boolean res = redis.opsForValue().setIfAbsent(key, txnId, ttl);
        return Boolean.TRUE.equals(res);
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }
}
