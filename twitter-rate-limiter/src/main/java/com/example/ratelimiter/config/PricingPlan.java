package com.example.ratelimiter.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import lombok.Getter;

import java.time.Duration;

/**
 * Defines the rate limit plans (buckets).
 * - Capacity: Max tokens (Burst size).
 * - Refill: Speed at which tokens are added back.
 */
@Getter
public enum PricingPlan {

    /**
     * Free User: 
     * - 20 requests per hour (Steady state).
     * - Can burst up to 5 requests instantly.
     */
    FREE(5, 20, Duration.ofHours(1)),

    /**
     * Premium User:
     * - 100 requests per hour.
     * - Can burst up to 20 requests instantly.
     */
    PREMIUM(20, 100, Duration.ofHours(1)),

    /**
     * Global System Limit (Abuse Prevention):
     * - 10,000 requests per minute across the entire system.
     * - Protects backend from DDoS.
     */
    GLOBAL_SYSTEM(1000, 10000, Duration.ofMinutes(1));

    private final int bucketCapacity;
    private final int refillTokens;
    private final Duration refillDuration;

    PricingPlan(int bucketCapacity, int refillTokens, Duration refillDuration) {
        this.bucketCapacity = bucketCapacity;
        this.refillTokens = refillTokens;
        this.refillDuration = refillDuration;
    }

    public Bandwidth getLimit() {
        return Bandwidth.classic(bucketCapacity, Refill.intervally(refillTokens, refillDuration));
    }
    
    public static PricingPlan resolvePlanFromApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return FREE;
        } else if (apiKey.startsWith("PREMIUM-")) {
            return PREMIUM;
        }
        return FREE;
    }
}
