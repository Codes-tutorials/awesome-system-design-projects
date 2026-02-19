package com.example.ratelimiter.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class HybridRateLimiter {

    private final RedisRateLimiter redisRateLimiter;

    // Local "Blacklist" Cache
    // If Redis says "User X is blocked for 10 seconds", we store this locally.
    // Next request checks this first and fails FAST without hitting Redis.
    private final Cache<String, Long> localBlockCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES) // Max TTL just in case
            .maximumSize(10000)
            .build();

    public RedisRateLimiter.RateLimitResult allowRequest(String userId) {
        // 1. Check Local Cache (Fast Fail)
        Long blockedUntil = localBlockCache.getIfPresent(userId);
        long now = System.currentTimeMillis() / 1000;
        
        if (blockedUntil != null) {
            if (now < blockedUntil) {
                log.debug("Local Block: User {} blocked until {}", userId, blockedUntil);
                return new RedisRateLimiter.RateLimitResult(false, 0, blockedUntil - now);
            } else {
                // Expired, remove
                localBlockCache.invalidate(userId);
            }
        }

        // 2. Check Redis (Global Truth)
        // Rule: 100 requests per minute = 100 capacity, refill 1.66/sec
        // Or strictly 100/min? Let's use 100 burst, 100/60 refill rate.
        double refillRate = 100.0 / 60.0;
        RedisRateLimiter.RateLimitResult result = redisRateLimiter.isAllowed(userId, 100, refillRate);

        // 3. Update Local Cache if blocked
        if (!result.allowed()) {
            // Redis says wait X seconds. Cache this.
            // Add a small buffer or cap it to avoid clock skew issues?
            // "retryAfter" from Redis is accurate.
            long retryAfter = result.retryAfter();
            if (retryAfter > 0) {
                localBlockCache.put(userId, now + retryAfter);
                log.info("Redis Block: Blocking user {} locally for {} seconds", userId, retryAfter);
            }
        }

        return result;
    }
}
