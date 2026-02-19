package com.example.retrysafe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    
    // Capacity: 10, Refill: 1 token/sec (for easy testing)
    private static final long CAPACITY = 10;
    private static final long REFILL_RATE = 1;

    private final RedisScript<List> acquireScript;
    private final RedisScript<Long> refundScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        DefaultRedisScript<List> s1 = new DefaultRedisScript<>();
        s1.setLocation(new ClassPathResource("scripts/acquire_token.lua"));
        s1.setResultType(List.class);
        this.acquireScript = s1;

        DefaultRedisScript<Long> s2 = new DefaultRedisScript<>();
        s2.setLocation(new ClassPathResource("scripts/refund_token.lua"));
        s2.setResultType(Long.class);
        this.refundScript = s2;
    }

    public RateLimitResult tryAcquire(String userId) {
        String key = "rate_limit:" + userId;
        long now = Instant.now().getEpochSecond();
        
        // ARGV: capacity, rate, now, requested
        List<Long> result = redisTemplate.execute(acquireScript, Collections.singletonList(key), 
                String.valueOf(CAPACITY), 
                String.valueOf(REFILL_RATE), 
                String.valueOf(now), 
                "1");

        boolean allowed = result.get(0) == 1L;
        long remaining = result.get(1);
        long retryAfter = result.get(2);

        return new RateLimitResult(allowed, remaining, retryAfter);
    }

    public void refundToken(String userId) {
        String key = "rate_limit:" + userId;
        Long newBalance = redisTemplate.execute(refundScript, Collections.singletonList(key), 
                String.valueOf(CAPACITY), 
                "1");
        log.info("Refunding token for user {}. New Balance: {}", userId, newBalance);
    }

    public record RateLimitResult(boolean allowed, long remaining, long retryAfter) {}
}
