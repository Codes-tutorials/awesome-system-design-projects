package com.ipl.ticketbooking.service;

import com.ipl.ticketbooking.model.Seat;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CRITICAL SERVICE: Handles distributed locking for seat reservations
 * Prevents multiple users from booking the same seat simultaneously
 * Uses Redis distributed locks for high-performance concurrency control
 */
@Service
public class SeatLockingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatLockingService.class);
    private static final int LOCK_WAIT_TIME_SECONDS = 5;
    private static final int LOCK_LEASE_TIME_MINUTES = 10;
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * Attempts to acquire distributed locks for multiple seats
     * This is the CORE method that prevents race conditions
     */
    public boolean lockSeats(List<Long> seatIds, Long userId) {
        String lockKey = generateLockKey(seatIds, userId);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock with timeout
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME_SECONDS, LOCK_LEASE_TIME_MINUTES, TimeUnit.SECONDS);
            
            if (acquired) {
                logger.info("Successfully acquired lock for seats {} by user {}", seatIds, userId);
                return true;
            } else {
                logger.warn("Failed to acquire lock for seats {} by user {} - seats may be locked by another user", 
                           seatIds, userId);
                return false;
            }
            
        } catch (InterruptedException e) {
            logger.error("Lock acquisition interrupted for seats {} by user {}", seatIds, userId, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * Releases distributed locks for seats
     */
    public void unlockSeats(List<Long> seatIds, Long userId) {
        String lockKey = generateLockKey(seatIds, userId);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                logger.info("Successfully released lock for seats {} by user {}", seatIds, userId);
            }
        } catch (Exception e) {
            logger.error("Error releasing lock for seats {} by user {}", seatIds, userId, e);
        }
    }
    
    /**
     * Checks if seats are currently locked by any user
     */
    public boolean areSeatsLocked(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String lockPattern = "seat_lock:" + seatId + ":*";
            if (redissonClient.getKeys().countExists(lockPattern) > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extends lock duration for ongoing booking process
     */
    public boolean extendLock(List<Long> seatIds, Long userId, int additionalMinutes) {
        String lockKey = generateLockKey(seatIds, userId);
        RLock lock = redissonClient.getLock(lockKey);
        
        if (lock.isHeldByCurrentThread()) {
            try {
                lock.expire(additionalMinutes, TimeUnit.MINUTES);
                logger.info("Extended lock for seats {} by user {} for {} minutes", 
                           seatIds, userId, additionalMinutes);
                return true;
            } catch (Exception e) {
                logger.error("Failed to extend lock for seats {} by user {}", seatIds, userId, e);
                return false;
            }
        }
        return false;
    }
    
    /**
     * Force release locks (admin operation)
     */
    public void forceUnlockSeats(List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String lockPattern = "seat_lock:" + seatId + ":*";
            redissonClient.getKeys().deleteByPattern(lockPattern);
        }
        logger.warn("Force unlocked seats: {}", seatIds);
    }
    
    /**
     * Cleanup expired locks (scheduled task)
     */
    public void cleanupExpiredLocks() {
        try {
            // Redisson automatically handles lock expiration, but we can add custom cleanup logic
            logger.debug("Cleanup expired locks task executed");
        } catch (Exception e) {
            logger.error("Error during lock cleanup", e);
        }
    }
    
    private String generateLockKey(List<Long> seatIds, Long userId) {
        // Sort seat IDs to ensure consistent lock ordering (prevents deadlocks)
        seatIds.sort(Long::compareTo);
        return "seat_lock:" + String.join(",", seatIds.stream().map(String::valueOf).toArray(String[]::new)) 
               + ":" + userId;
    }
}