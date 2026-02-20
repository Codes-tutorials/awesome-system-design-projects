package com.ecommerce.order.service;

import com.ecommerce.order.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // Lua script for atomic rate limiting operations
    private static final String RATE_LIMIT_SCRIPT = """
        local key = KEYS[1]
        local window = tonumber(ARGV[1])
        local limit = tonumber(ARGV[2])
        local current_time = tonumber(ARGV[3])
        
        -- Remove expired entries
        redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window * 1000)
        
        -- Count current requests
        local current_requests = redis.call('ZCARD', key)
        
        if current_requests < limit then
            -- Add current request
            redis.call('ZADD', key, current_time, current_time)
            redis.call('EXPIRE', key, window)
            return {1, limit - current_requests - 1}
        else
            return {0, 0}
        end
        """;
    
    // Token bucket Lua script for smooth rate limiting
    private static final String TOKEN_BUCKET_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local tokens = tonumber(ARGV[2])
        local interval = tonumber(ARGV[3])
        local requested = tonumber(ARGV[4])
        local current_time = tonumber(ARGV[5])
        
        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local current_tokens = tonumber(bucket[1]) or capacity
        local last_refill = tonumber(bucket[2]) or current_time
        
        -- Calculate tokens to add based on time elapsed
        local time_passed = current_time - last_refill
        local tokens_to_add = math.floor(time_passed / interval * tokens)
        current_tokens = math.min(capacity, current_tokens + tokens_to_add)
        
        if current_tokens >= requested then
            current_tokens = current_tokens - requested
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, capacity * interval / tokens)
            return {1, current_tokens}
        else
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, capacity * interval / tokens)
            return {0, current_tokens}
        end
        """;
    
    @Autowired
    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Check rate limit using sliding window algorithm
     */
    public boolean checkRateLimit(String key, int limit, int windowSeconds) {
        return checkRateLimit(key, limit, windowSeconds, false);
    }
    
    /**
     * Check rate limit with option to throw exception
     */
    public boolean checkRateLimit(String key, int limit, int windowSeconds, boolean throwOnExceed) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) redisTemplate.execute(
                RedisScript.of(RATE_LIMIT_SCRIPT, List.class),
                Collections.singletonList(redisKey),
                String.valueOf(windowSeconds),
                String.valueOf(limit),
                String.valueOf(currentTime)
            );
            
            boolean allowed = result.get(0) == 1;
            long remaining = result.get(1);
            
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {}, limit: {}, window: {}s", key, limit, windowSeconds);
                if (throwOnExceed) {
                    throw new RateLimitExceededException("Rate limit exceeded for: " + key);
                }
            } else {
                log.debug("Rate limit check passed for key: {}, remaining: {}", key, remaining);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request if Redis is unavailable
            return true;
        }
    }
    
    /**
     * Check rate limit using token bucket algorithm for smooth rate limiting
     */
    public boolean checkTokenBucketRateLimit(String key, int capacity, int tokensPerSecond, int requestedTokens) {
        String redisKey = "token_bucket:" + key;
        long currentTime = System.currentTimeMillis();
        int intervalMs = 1000; // 1 second interval
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) redisTemplate.execute(
                RedisScript.of(TOKEN_BUCKET_SCRIPT, List.class),
                Collections.singletonList(redisKey),
                String.valueOf(capacity),
                String.valueOf(tokensPerSecond),
                String.valueOf(intervalMs),
                String.valueOf(requestedTokens),
                String.valueOf(currentTime)
            );
            
            boolean allowed = result.get(0) == 1;
            long remainingTokens = result.get(1);
            
            if (!allowed) {
                log.warn("Token bucket rate limit exceeded for key: {}, capacity: {}, tokens/sec: {}", 
                        key, capacity, tokensPerSecond);
            } else {
                log.debug("Token bucket rate limit check passed for key: {}, remaining tokens: {}", 
                         key, remainingTokens);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error checking token bucket rate limit for key: {}", key, e);
            return true; // Fail open
        }
    }
    
    /**
     * Check user-specific rate limit for flash sales
     */
    public void checkUserRateLimit(String userId, String operation) {
        String key = String.format("user:%s:%s", userId, operation);
        
        // Different limits based on operation
        switch (operation) {
            case "flash-sale" -> {
                // Very restrictive for flash sales: 1 order per minute
                if (!checkRateLimit(key, 1, 60, true)) {
                    throw new RateLimitExceededException("Flash sale rate limit exceeded for user: " + userId);
                }
            }
            case "create-order" -> {
                // Normal order creation: 10 orders per minute
                if (!checkRateLimit(key, 10, 60, true)) {
                    throw new RateLimitExceededException("Order creation rate limit exceeded for user: " + userId);
                }
            }
            case "api-call" -> {
                // General API calls: 100 per minute
                if (!checkRateLimit(key, 100, 60, true)) {
                    throw new RateLimitExceededException("API rate limit exceeded for user: " + userId);
                }
            }
            default -> {
                // Default rate limit: 50 per minute
                if (!checkRateLimit(key, 50, 60, true)) {
                    throw new RateLimitExceededException("Rate limit exceeded for user: " + userId);
                }
            }
        }
    }
    
    /**
     * Check global rate limit for system protection
     */
    public boolean checkGlobalRateLimit(String operation, int limit, int windowSeconds) {
        String key = "global:" + operation;
        return checkRateLimit(key, limit, windowSeconds);
    }
    
    /**
     * Check IP-based rate limit
     */
    public boolean checkIpRateLimit(String ipAddress, int limit, int windowSeconds) {
        String key = "ip:" + ipAddress;
        return checkRateLimit(key, limit, windowSeconds);
    }
    
    /**
     * Get current rate limit status
     */
    public RateLimitStatus getRateLimitStatus(String key, int limit, int windowSeconds) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (windowSeconds * 1000L);
        
        try {
            Long currentRequests = redisTemplate.opsForZSet().count(redisKey, windowStart, currentTime);
            long remaining = Math.max(0, limit - currentRequests);
            long resetTime = currentTime + (windowSeconds * 1000L);
            
            return new RateLimitStatus(
                limit,
                currentRequests.intValue(),
                (int) remaining,
                Instant.ofEpochMilli(resetTime)
            );
            
        } catch (Exception e) {
            log.error("Error getting rate limit status for key: {}", key, e);
            return new RateLimitStatus(limit, 0, limit, Instant.now().plus(Duration.ofSeconds(windowSeconds)));
        }
    }
    
    /**
     * Reset rate limit for a specific key
     */
    public void resetRateLimit(String key) {
        try {
            String redisKey = "rate_limit:" + key;
            redisTemplate.delete(redisKey);
            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
    
    /**
     * Increment rate limit counter manually
     */
    public void incrementRateLimit(String key, int windowSeconds) {
        String redisKey = "rate_limit:" + key;
        long currentTime = System.currentTimeMillis();
        
        try {
            redisTemplate.opsForZSet().add(redisKey, String.valueOf(currentTime), currentTime);
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        } catch (Exception e) {
            log.error("Error incrementing rate limit for key: {}", key, e);
        }
    }
    
    /**
     * Rate limit status information
     */
    public static class RateLimitStatus {
        private final int limit;
        private final int current;
        private final int remaining;
        private final Instant resetTime;
        
        public RateLimitStatus(int limit, int current, int remaining, Instant resetTime) {
            this.limit = limit;
            this.current = current;
            this.remaining = remaining;
            this.resetTime = resetTime;
        }
        
        public int getLimit() { return limit; }
        public int getCurrent() { return current; }
        public int getRemaining() { return remaining; }
        public Instant getResetTime() { return resetTime; }
        
        public boolean isExceeded() { return remaining <= 0; }
        
        @Override
        public String toString() {
            return String.format("RateLimitStatus{limit=%d, current=%d, remaining=%d, resetTime=%s}", 
                               limit, current, remaining, resetTime);
        }
    }
}