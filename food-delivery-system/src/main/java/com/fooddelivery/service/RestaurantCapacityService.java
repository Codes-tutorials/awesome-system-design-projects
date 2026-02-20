package com.fooddelivery.service;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Service to manage restaurant capacity and load balancing
 */
@Service
public class RestaurantCapacityService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CAPACITY_KEY = "restaurant_capacity:";
    private static final String CURRENT_LOAD_KEY = "restaurant_load:";
    private static final String BURST_CAPACITY_KEY = "burst_capacity:";
    private static final String PREPARATION_TIME_KEY = "prep_time:";
    
    public boolean hasCapacity(Long restaurantId) {
        int currentLoad = getCurrentLoad(restaurantId);
        int maxCapacity = getMaxCapacity(restaurantId);
        
        // Check if in burst mode
        if (isBurstMode(restaurantId)) {
            maxCapacity = getBurstCapacity(restaurantId);
        }
        
        return currentLoad < maxCapacity;
    }
    
    public int getMaxCapacity(Long restaurantId) {
        String capacityKey = CAPACITY_KEY + restaurantId;
        Integer capacity = (Integer) redisTemplate.opsForValue().get(capacityKey);
        
        if (capacity == null) {
            // Calculate base capacity based on restaurant size and historical data
            capacity = calculateBaseCapacity(restaurantId);
            redisTemplate.opsForValue().set(capacityKey, capacity, 1, TimeUnit.HOURS);
        }
        
        return capacity;
    }
    
    public int getBurstCapacity(Long restaurantId) {
        String burstKey = BURST_CAPACITY_KEY + restaurantId;
        Integer burstCapacity = (Integer) redisTemplate.opsForValue().get(burstKey);
        
        if (burstCapacity == null) {
            // Burst capacity is typically 150% of normal capacity
            int normalCapacity = getMaxCapacity(restaurantId);
            burstCapacity = (int) (normalCapacity * 1.5);
            redisTemplate.opsForValue().set(burstKey, burstCapacity, 30, TimeUnit.MINUTES);
        }
        
        return burstCapacity;
    }
    
    public int getCurrentLoad(Long restaurantId) {
        String loadKey = CURRENT_LOAD_KEY + restaurantId;
        Integer load = (Integer) redisTemplate.opsForValue().get(loadKey);
        return load != null ? load : 0;
    }
    
    public void enableBurstMode(Long restaurantId) {
        redisTemplate.opsForValue().set("burst_mode:" + restaurantId, true, 30, TimeUnit.MINUTES);
        
        // Notify restaurant of burst mode
        kafkaTemplate.send("restaurant-notifications", createBurstModeNotification(restaurantId, true));
        
        // Adjust preparation times
        adjustPreparationTimes(restaurantId, true);
    }
    
    public void disableBurstMode(Long restaurantId) {
        redisTemplate.delete("burst_mode:" + restaurantId);
        
        // Notify restaurant
        kafkaTemplate.send("restaurant-notifications", createBurstModeNotification(restaurantId, false));
        
        // Reset preparation times
        adjustPreparationTimes(restaurantId, false);
    }
    
    public boolean isBurstMode(Long restaurantId) {
        Boolean burstMode = (Boolean) redisTemplate.opsForValue().get("burst_mode:" + restaurantId);
        return burstMode != null && burstMode;
    }
    
    public void updateCapacityBasedOnPerformance(Long restaurantId) {
        // Analyze recent performance metrics
        double avgPreparationTime = getAveragePreparationTime(restaurantId);
        double orderCompletionRate = getOrderCompletionRate(restaurantId);
        int currentCapacity = getMaxCapacity(restaurantId);
        
        int newCapacity = currentCapacity;
        
        // Increase capacity if performance is good
        if (avgPreparationTime < 15 && orderCompletionRate > 0.95) {
            newCapacity = (int) (currentCapacity * 1.1);
        }
        // Decrease capacity if performance is poor
        else if (avgPreparationTime > 25 || orderCompletionRate < 0.85) {
            newCapacity = (int) (currentCapacity * 0.9);
        }
        
        // Ensure minimum capacity
        newCapacity = Math.max(newCapacity, 5);
        
        if (newCapacity != currentCapacity) {
            updateCapacity(restaurantId, newCapacity);
        }
    }
    
    public void updateCapacity(Long restaurantId, int newCapacity) {
        String capacityKey = CAPACITY_KEY + restaurantId;
        redisTemplate.opsForValue().set(capacityKey, newCapacity, 1, TimeUnit.HOURS);
        
        // Log capacity change
        kafkaTemplate.send("capacity-changes", createCapacityChangeEvent(restaurantId, newCapacity));
    }
    
    public int getEstimatedWaitTime(Long restaurantId) {
        int queueLength = getCurrentLoad(restaurantId);
        double avgPreparationTime = getAveragePreparationTime(restaurantId);
        
        // Calculate estimated wait time based on queue and preparation time
        return (int) (queueLength * avgPreparationTime);
    }
    
    public boolean shouldThrottleOrders(Long restaurantId) {
        int currentLoad = getCurrentLoad(restaurantId);
        int maxCapacity = getMaxCapacity(restaurantId);
        
        // Start throttling at 80% capacity
        return (double) currentLoad / maxCapacity > 0.8;
    }
    
    public void adjustCapacityForTimeOfDay(Long restaurantId) {
        LocalTime currentTime = LocalTime.now();
        int baseCapacity = calculateBaseCapacity(restaurantId);
        
        // Adjust capacity based on time of day
        double multiplier = 1.0;
        
        // Lunch rush (11:30 AM - 2:30 PM)
        if (currentTime.isAfter(LocalTime.of(11, 30)) && currentTime.isBefore(LocalTime.of(14, 30))) {
            multiplier = 1.3;
        }
        // Dinner rush (6:00 PM - 9:00 PM)
        else if (currentTime.isAfter(LocalTime.of(18, 0)) && currentTime.isBefore(LocalTime.of(21, 0))) {
            multiplier = 1.4;
        }
        // Late night (10:00 PM - 2:00 AM)
        else if (currentTime.isAfter(LocalTime.of(22, 0)) || currentTime.isBefore(LocalTime.of(2, 0))) {
            multiplier = 0.7;
        }
        
        int adjustedCapacity = (int) (baseCapacity * multiplier);
        updateCapacity(restaurantId, adjustedCapacity);
    }
    
    private int calculateBaseCapacity(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
        if (restaurant == null) {
            return 10; // Default capacity
        }
        
        // Base capacity calculation based on various factors
        int baseCapacity = 15; // Default
        
        // Adjust based on restaurant rating
        if (restaurant.getAverageRating().doubleValue() > 4.5) {
            baseCapacity += 5;
        }
        
        // Adjust based on total orders (experience)
        // This would require additional data tracking
        
        return baseCapacity;
    }
    
    private double getAveragePreparationTime(Long restaurantId) {
        String prepTimeKey = PREPARATION_TIME_KEY + restaurantId;
        Double avgTime = (Double) redisTemplate.opsForValue().get(prepTimeKey);
        return avgTime != null ? avgTime : 20.0; // Default 20 minutes
    }
    
    private double getOrderCompletionRate(Long restaurantId) {
        // This would calculate completion rate from recent orders
        // For now, return a default value
        return 0.92;
    }
    
    private void adjustPreparationTimes(Long restaurantId, boolean burstMode) {
        String prepTimeKey = PREPARATION_TIME_KEY + restaurantId;
        double currentPrepTime = getAveragePreparationTime(restaurantId);
        
        if (burstMode) {
            // Increase preparation time estimates during burst
            double adjustedTime = currentPrepTime * 1.2;
            redisTemplate.opsForValue().set(prepTimeKey, adjustedTime, 30, TimeUnit.MINUTES);
        } else {
            // Reset to normal
            redisTemplate.delete(prepTimeKey);
        }
    }
    
    private Object createBurstModeNotification(Long restaurantId, boolean enabled) {
        return new Object() {
            public final String notificationType = "BURST_MODE_CHANGE";
            public final Long restaurantId = restaurantId;
            public final boolean burstModeEnabled = enabled;
            public final Long timestamp = System.currentTimeMillis();
            public final String message = enabled ? 
                "Burst mode enabled - increased capacity and preparation times" :
                "Burst mode disabled - returning to normal operations";
        };
    }
    
    private Object createCapacityChangeEvent(Long restaurantId, int newCapacity) {
        return new Object() {
            public final String eventType = "CAPACITY_CHANGE";
            public final Long restaurantId = restaurantId;
            public final int newCapacity = newCapacity;
            public final Long timestamp = System.currentTimeMillis();
        };
    }
}