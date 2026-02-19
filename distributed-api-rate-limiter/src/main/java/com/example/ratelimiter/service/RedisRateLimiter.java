package com.example.ratelimiter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private DefaultRedisScript<List> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua")));
        redisScript.setResultType(List.class);
    }

    /**
     * Tries to acquire tokens from Redis.
     * 
     * @param userId User Identifier
     * @param capacity Max burst capacity (e.g., 100)
     * @param refillRate Tokens per second (e.g., 100/60 = 1.66)
     * @return RateLimitResult
     */
    public RateLimitResult isAllowed(String userId, int capacity, double refillRate) {
        String key = "rate_limit:" + userId;
        long now = Instant.now().getEpochSecond();
        
        // Execute Lua Script
        // Returns: [allowed (0/1), remaining_tokens, retry_after_seconds]
        List<Object> result = redisTemplate.execute(redisScript, Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(now),
                "1" // requested tokens
        );

        if (result != null && !result.isEmpty()) {
            boolean allowed = ((Long) result.get(0)) == 1L;
            // Handle double/long conversion safely
            double remaining = Double.parseDouble(result.get(1).toString());
            double retryAfter = Double.parseDouble(result.get(2).toString());
            
            return new RateLimitResult(allowed, (long) remaining, (long) retryAfter);
        }
        
        // Fallback in case of Redis error (fail open or closed?) -> fail open here
        log.error("Redis Rate Limiter script returned null");
        return new RateLimitResult(true, 0, 0);
    }

    public record RateLimitResult(boolean allowed, long remaining, long retryAfter) {}
}
