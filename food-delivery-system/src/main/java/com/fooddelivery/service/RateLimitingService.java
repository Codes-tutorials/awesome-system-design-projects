package com.fooddelivery.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Advanced rate limiting service for handling burst traffic
 */
@Service
public class RateLimitingService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Rate limiting configurations
    private static final int ORDERS_PER_MINUTE_NORMAL = 10;
    private static final int ORDERS_PER_MINUTE_BURST = 20;
    private static final int ORDERS_PER_HOUR_NORMAL = 100;
    private static final int ORDERS_PER_HOUR_BURST = 200;
    
    public boolean isAllowed(String userId, String action) {
        String key = userId + ":" + action;
        Bucket bucket = getBucket(key, false);
        return bucket.tryConsume(1);
    }
    
    public boolean isAllowedWithBurst(String userId, String action) {
        String key = userId + ":" + action;
        Bucket bucket = getBucket(key, true);
        return bucket.tryConsume(1);
    }
    
    public boolean isRestaurantOrderAllowed(Long restaurantId, Long userId) {
        // Check user-specific rate limit
        if (!isAllowed(userId.toString(), "order")) {
            return false;
        }
        
        // Check restaurant-specific rate limit
        String restaurantKey = "restaurant:" + restaurantId;
        return isRestaurantCapacityAvailable(restaurantKey);
    }
    
    public boolean isRestaurantCapacityAvailable(String restaurantKey) {
        Bucket bucket = getRestaurantBucket(restaurantKey);
        return bucket.tryConsume(1);
    }
    
    public void enableBurstMode(String userId) {
        String burstKey = "burst_mode:" + userId;
        redisTemplate.opsForValue().set(burstKey, true, 10, TimeUnit.MINUTES);
        
        // Clear existing buckets to apply new limits
        buckets.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
    }
    
    public void enableGlobalBurstMode() {
        redisTemplate.opsForValue().set("global_burst_mode", true, 30, TimeUnit.MINUTES);
        
        // Clear all buckets to apply burst limits
        buckets.clear();
    }
    
    public boolean isInBurstMode(String userId) {
        String burstKey = "burst_mode:" + userId;
        Boolean burstMode = (Boolean) redisTemplate.opsForValue().get(burstKey);
        return burstMode != null && burstMode;
    }
    
    public boolean isGlobalBurstMode() {
        Boolean globalBurst = (Boolean) redisTemplate.opsForValue().get("global_burst_mode");
        return globalBurst != null && globalBurst;
    }
    
    private Bucket getBucket(String key, boolean burstMode) {
        return buckets.computeIfAbsent(key, k -> createBucket(k, burstMode));
    }
    
    private Bucket getRestaurantBucket(String restaurantKey) {
        return buckets.computeIfAbsent(restaurantKey, k -> createRestaurantBucket());
    }
    
    private Bucket createBucket(String key, boolean burstMode) {
        String userId = key.split(":")[0];
        boolean userBurstMode = burstMode || isInBurstMode(userId) || isGlobalBurstMode();
        
        int ordersPerMinute = userBurstMode ? ORDERS_PER_MINUTE_BURST : ORDERS_PER_MINUTE_NORMAL;
        int ordersPerHour = userBurstMode ? ORDERS_PER_HOUR_BURST : ORDERS_PER_HOUR_NORMAL;
        
        // Create bucket with multiple bandwidth limits
        Bandwidth minuteLimit = Bandwidth.classic(ordersPerMinute, Refill.intervally(ordersPerMinute, Duration.ofMinutes(1)));
        Bandwidth hourLimit = Bandwidth.classic(ordersPerHour, Refill.intervally(ordersPerHour, Duration.ofHours(1)));
        
        return Bucket4j.builder()
                .addLimit(minuteLimit)
                .addLimit(hourLimit)
                .build();
    }
    
    private Bucket createRestaurantBucket() {
        // Restaurant capacity bucket - 100 orders per minute
        Bandwidth restaurantLimit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        
        return Bucket4j.builder()
                .addLimit(restaurantLimit)
                .build();
    }
    
    public void applyPenalty(String userId, int penaltyMinutes) {
        String penaltyKey = "penalty:" + userId;
        redisTemplate.opsForValue().set(penaltyKey, true, penaltyMinutes, TimeUnit.MINUTES);
        
        // Remove user's buckets to apply penalty immediately
        buckets.entrySet().removeIf(entry -> entry.getKey().startsWith(userId + ":"));
    }
    
    public boolean isUnderPenalty(String userId) {
        String penaltyKey = "penalty:" + userId;
        Boolean penalty = (Boolean) redisTemplate.opsForValue().get(penaltyKey);
        return penalty != null && penalty;
    }
    
    public long getRemainingTokens(String userId, String action) {
        String key = userId + ":" + action;
        Bucket bucket = buckets.get(key);
        
        if (bucket != null) {
            return bucket.getAvailableTokens();
        }
        
        return 0;
    }
    
    public void incrementFailedAttempts(String userId) {
        String failedKey = "failed_attempts:" + userId;
        Long attempts = redisTemplate.opsForValue().increment(failedKey);
        redisTemplate.expire(failedKey, 1, TimeUnit.HOURS);
        
        // Apply progressive penalties
        if (attempts != null) {
            if (attempts >= 10) {
                applyPenalty(userId, 60); // 1 hour penalty
            } else if (attempts >= 5) {
                applyPenalty(userId, 15); // 15 minute penalty
            }
        }
    }
    
    public void resetFailedAttempts(String userId) {
        String failedKey = "failed_attempts:" + userId;
        redisTemplate.delete(failedKey);
    }
    
    public boolean isIPBlocked(String ipAddress) {
        String blockKey = "blocked_ip:" + ipAddress;
        Boolean blocked = (Boolean) redisTemplate.opsForValue().get(blockKey);
        return blocked != null && blocked;
    }
    
    public void blockIP(String ipAddress, int durationMinutes) {
        String blockKey = "blocked_ip:" + ipAddress;
        redisTemplate.opsForValue().set(blockKey, true, durationMinutes, TimeUnit.MINUTES);
    }
    
    public void detectAndHandleAbuse(String userId, String ipAddress) {
        // Check for rapid successive requests
        String rapidKey = "rapid_requests:" + userId;
        Long requestCount = redisTemplate.opsForValue().increment(rapidKey);
        
        if (requestCount == 1) {
            redisTemplate.expire(rapidKey, 10, TimeUnit.SECONDS);
        }
        
        // If more than 20 requests in 10 seconds, apply penalty
        if (requestCount != null && requestCount > 20) {
            applyPenalty(userId, 30);
            
            // If severe abuse, block IP
            if (requestCount > 50) {
                blockIP(ipAddress, 60);
            }
        }
    }
    
    public RateLimitInfo getRateLimitInfo(String userId, String action) {
        String key = userId + ":" + action;
        Bucket bucket = buckets.get(key);
        
        RateLimitInfo info = new RateLimitInfo();
        info.setUserId(userId);
        info.setAction(action);
        info.setInBurstMode(isInBurstMode(userId));
        info.setUnderPenalty(isUnderPenalty(userId));
        
        if (bucket != null) {
            info.setRemainingTokens(bucket.getAvailableTokens());
        }
        
        return info;
    }
    
    // DTO class for rate limit information
    public static class RateLimitInfo {
        private String userId;
        private String action;
        private long remainingTokens;
        private boolean inBurstMode;
        private boolean underPenalty;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public long getRemainingTokens() { return remainingTokens; }
        public void setRemainingTokens(long remainingTokens) { this.remainingTokens = remainingTokens; }
        
        public boolean isInBurstMode() { return inBurstMode; }
        public void setInBurstMode(boolean inBurstMode) { this.inBurstMode = inBurstMode; }
        
        public boolean isUnderPenalty() { return underPenalty; }
        public void setUnderPenalty(boolean underPenalty) { this.underPenalty = underPenalty; }
    }
}