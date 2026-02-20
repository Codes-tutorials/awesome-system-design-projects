package com.ipl.ticketbooking.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * RATE LIMITING SERVICE: Prevents system overload during burst traffic
 * Uses token bucket algorithm with Redis for distributed rate limiting
 * Essential for handling millions of concurrent users during ticket sales
 */
@Service
public class RateLimitingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);
    
    @Autowired
    private ProxyManager<String> proxyManager;
    
    // Rate limiting configurations for different operations
    private static final int BOOKING_REQUESTS_PER_MINUTE = 5;
    private static final int SEARCH_REQUESTS_PER_MINUTE = 30;
    private static final int LOGIN_REQUESTS_PER_MINUTE = 10;
    
    /**
     * Checks if a user is allowed to perform a specific operation
     * Uses distributed token bucket for fair rate limiting across all instances
     */
    public boolean isAllowed(Long userId, String operation) {
        String bucketKey = generateBucketKey(userId, operation);
        
        try {
            Bucket bucket = proxyManager.builder()
                .build(bucketKey, getBucketConfiguration(operation));
            
            boolean allowed = bucket.tryConsume(1);
            
            if (!allowed) {
                logger.warn("Rate limit exceeded for user {} on operation {}", userId, operation);
            }
            
            return allowed;
            
        } catch (Exception e) {
            logger.error("Error checking rate limit for user {} on operation {}", userId, operation, e);
            // Fail open - allow request if rate limiting service fails
            return true;
        }
    }
    
    /**
     * Checks if a user is allowed with custom token consumption
     */
    public boolean isAllowed(Long userId, String operation, int tokens) {
        String bucketKey = generateBucketKey(userId, operation);
        
        try {
            Bucket bucket = proxyManager.builder()
                .build(bucketKey, getBucketConfiguration(operation));
            
            return bucket.tryConsume(tokens);
            
        } catch (Exception e) {
            logger.error("Error checking rate limit for user {} on operation {} with {} tokens", 
                        userId, operation, tokens, e);
            return true;
        }
    }
    
    /**
     * Gets remaining tokens for a user's bucket
     */
    public long getRemainingTokens(Long userId, String operation) {
        String bucketKey = generateBucketKey(userId, operation);
        
        try {
            Bucket bucket = proxyManager.builder()
                .build(bucketKey, getBucketConfiguration(operation));
            
            return bucket.getAvailableTokens();
            
        } catch (Exception e) {
            logger.error("Error getting remaining tokens for user {} on operation {}", userId, operation, e);
            return 0;
        }
    }
    
    /**
     * Global rate limiting for system-wide operations (e.g., during flash sales)
     */
    public boolean isGloballyAllowed(String operation) {
        String bucketKey = "global:" + operation;
        
        try {
            Bucket bucket = proxyManager.builder()
                .build(bucketKey, getGlobalBucketConfiguration(operation));
            
            return bucket.tryConsume(1);
            
        } catch (Exception e) {
            logger.error("Error checking global rate limit for operation {}", operation, e);
            return true;
        }
    }
    
    /**
     * IP-based rate limiting for additional protection
     */
    public boolean isAllowedByIP(String ipAddress, String operation) {
        String bucketKey = "ip:" + ipAddress + ":" + operation;
        
        try {
            Bucket bucket = proxyManager.builder()
                .build(bucketKey, getIPBucketConfiguration(operation));
            
            boolean allowed = bucket.tryConsume(1);
            
            if (!allowed) {
                logger.warn("IP rate limit exceeded for {} on operation {}", ipAddress, operation);
            }
            
            return allowed;
            
        } catch (Exception e) {
            logger.error("Error checking IP rate limit for {} on operation {}", ipAddress, operation, e);
            return true;
        }
    }
    
    /**
     * Burst protection during high-traffic events
     */
    public boolean isBurstAllowed(Long userId, String operation) {
        String bucketKey = generateBucketKey(userId, operation + ":burst");
        
        try {
            // More restrictive limits during burst periods
            BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(1, Duration.ofSeconds(10))) // 1 request per 10 seconds
                .build();
            
            Bucket bucket = proxyManager.builder().build(bucketKey, () -> config);
            
            return bucket.tryConsume(1);
            
        } catch (Exception e) {
            logger.error("Error checking burst rate limit for user {} on operation {}", userId, operation, e);
            return true;
        }
    }
    
    /**
     * Resets rate limit for a user (admin operation)
     */
    public void resetRateLimit(Long userId, String operation) {
        String bucketKey = generateBucketKey(userId, operation);
        
        try {
            proxyManager.removeProxy(bucketKey);
            logger.info("Reset rate limit for user {} on operation {}", userId, operation);
            
        } catch (Exception e) {
            logger.error("Error resetting rate limit for user {} on operation {}", userId, operation, e);
        }
    }
    
    private String generateBucketKey(Long userId, String operation) {
        return "rate_limit:user:" + userId + ":" + operation;
    }
    
    private Supplier<BucketConfiguration> getBucketConfiguration(String operation) {
        return () -> {
            switch (operation.toLowerCase()) {
                case "booking":
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(BOOKING_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                        .addLimit(Bandwidth.simple(1, Duration.ofSeconds(10))) // Burst protection
                        .build();
                        
                case "search":
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(SEARCH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                        .build();
                        
                case "login":
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(LOGIN_REQUESTS_PER_MINUTE, Duration.ofMinutes(1)))
                        .addLimit(Bandwidth.simple(3, Duration.ofMinutes(5))) // Additional protection
                        .build();
                        
                default:
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(20, Duration.ofMinutes(1)))
                        .build();
            }
        };
    }
    
    private Supplier<BucketConfiguration> getGlobalBucketConfiguration(String operation) {
        return () -> {
            switch (operation.toLowerCase()) {
                case "booking":
                    // Global limit: 10,000 bookings per minute across all users
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(10000, Duration.ofMinutes(1)))
                        .addLimit(Bandwidth.simple(200, Duration.ofSeconds(1))) // Burst protection
                        .build();
                        
                default:
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(50000, Duration.ofMinutes(1)))
                        .build();
            }
        };
    }
    
    private Supplier<BucketConfiguration> getIPBucketConfiguration(String operation) {
        return () -> {
            switch (operation.toLowerCase()) {
                case "booking":
                    // Per IP: 50 bookings per minute (to handle multiple users behind NAT)
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(50, Duration.ofMinutes(1)))
                        .addLimit(Bandwidth.simple(5, Duration.ofSeconds(10)))
                        .build();
                        
                case "login":
                    // Per IP: 100 login attempts per minute
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
                        .addLimit(Bandwidth.simple(10, Duration.ofMinutes(5)))
                        .build();
                        
                default:
                    return BucketConfiguration.builder()
                        .addLimit(Bandwidth.simple(200, Duration.ofMinutes(1)))
                        .build();
            }
        };
    }
}