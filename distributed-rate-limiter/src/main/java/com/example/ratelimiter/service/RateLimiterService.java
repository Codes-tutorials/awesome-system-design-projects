package com.example.ratelimiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Boolean> rateLimiterScript;

    /**
     * Checks if a request is allowed using the Token Bucket algorithm via Lua.
     *
     * @param key      Unique key for the limit (e.g., "user:123" or "global")
     * @param rate     Refill rate (tokens per second)
     * @param capacity Max burst capacity
     * @return true if allowed, false otherwise
     */
    public boolean isAllowed(String key, double rate, double capacity) {
        String redisKey = "rate_limit:" + key;
        long now = Instant.now().getEpochSecond();
        
        // Execute Lua script
        // ARGV[1]: Refill rate
        // ARGV[2]: Bucket capacity
        // ARGV[3]: Requested tokens (1)
        // ARGV[4]: Current timestamp
        return Boolean.TRUE.equals(redisTemplate.execute(
                rateLimiterScript,
                Collections.singletonList(redisKey),
                String.valueOf(rate),
                String.valueOf(capacity),
                "1",
                String.valueOf(now)
        ));
    }
}
