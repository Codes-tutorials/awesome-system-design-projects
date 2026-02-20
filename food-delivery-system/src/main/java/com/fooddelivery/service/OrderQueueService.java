package com.fooddelivery.service;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderPriority;
import com.fooddelivery.model.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle order queuing and burst traffic management
 */
@Service
public class OrderQueueService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private RestaurantCapacityService restaurantCapacityService;
    
    private static final String ORDER_QUEUE_KEY = "order_queue:";
    private static final String RESTAURANT_LOAD_KEY = "restaurant_load:";
    private static final String BURST_DETECTION_KEY = "burst_detection:";
    
    public boolean canAcceptOrder(Long restaurantId, Order order) {
        // Check restaurant capacity
        if (!restaurantCapacityService.hasCapacity(restaurantId)) {
            return false;
        }
        
        // Check current load
        String loadKey = RESTAURANT_LOAD_KEY + restaurantId;
        Integer currentLoad = (Integer) redisTemplate.opsForValue().get(loadKey);
        
        if (currentLoad == null) {
            currentLoad = 0;
        }
        
        // Get restaurant max capacity
        int maxCapacity = restaurantCapacityService.getMaxCapacity(restaurantId);
        
        return currentLoad < maxCapacity;
    }
    
    public void queueOrder(Order order, OrderPriority priority) {
        String queueKey = ORDER_QUEUE_KEY + order.getRestaurant().getId() + ":" + priority.name();
        
        // Add order to priority queue
        redisTemplate.opsForZSet().add(queueKey, order.getId(), System.currentTimeMillis());
        
        // Update restaurant load
        incrementRestaurantLoad(order.getRestaurant().getId());
        
        // Send to Kafka for async processing
        kafkaTemplate.send("order-queue", createOrderQueueMessage(order, priority));
        
        // Set TTL for queue entry (30 minutes)
        redisTemplate.expire(queueKey, 30, TimeUnit.MINUTES);
    }
    
    public void processOrderQueue(Long restaurantId) {
        // Process high priority orders first
        processQueueByPriority(restaurantId, OrderPriority.HIGH);
        processQueueByPriority(restaurantId, OrderPriority.MEDIUM);
        processQueueByPriority(restaurantId, OrderPriority.LOW);
    }
    
    private void processQueueByPriority(Long restaurantId, OrderPriority priority) {
        String queueKey = ORDER_QUEUE_KEY + restaurantId + ":" + priority.name();
        
        // Get oldest order from queue
        var orders = redisTemplate.opsForZSet().range(queueKey, 0, 0);
        
        if (orders != null && !orders.isEmpty()) {
            Long orderId = (Long) orders.iterator().next();
            
            // Check if restaurant can handle the order
            if (restaurantCapacityService.hasCapacity(restaurantId)) {
                // Remove from queue and process
                redisTemplate.opsForZSet().remove(queueKey, orderId);
                
                // Send for processing
                kafkaTemplate.send("order-processing", orderId);
            }
        }
    }
    
    public void detectBurstTraffic(Long restaurantId) {
        String burstKey = BURST_DETECTION_KEY + restaurantId;
        String timeWindow = String.valueOf(System.currentTimeMillis() / 60000); // 1-minute windows
        
        // Increment order count for current minute
        String countKey = burstKey + ":" + timeWindow;
        Long orderCount = redisTemplate.opsForValue().increment(countKey);
        redisTemplate.expire(countKey, 5, TimeUnit.MINUTES);
        
        // Check if burst threshold exceeded (e.g., 50 orders per minute)
        if (orderCount > 50) {
            handleBurstTraffic(restaurantId, orderCount);
        }
    }
    
    private void handleBurstTraffic(Long restaurantId, Long orderCount) {
        // Enable burst mode
        redisTemplate.opsForValue().set("burst_mode:" + restaurantId, true, 10, TimeUnit.MINUTES);
        
        // Increase restaurant capacity temporarily
        restaurantCapacityService.enableBurstMode(restaurantId);
        
        // Send alert to restaurant
        kafkaTemplate.send("restaurant-alerts", createBurstAlert(restaurantId, orderCount));
        
        // Notify operations team
        kafkaTemplate.send("ops-alerts", createOpsAlert(restaurantId, orderCount));
    }
    
    public void incrementRestaurantLoad(Long restaurantId) {
        String loadKey = RESTAURANT_LOAD_KEY + restaurantId;
        redisTemplate.opsForValue().increment(loadKey);
        redisTemplate.expire(loadKey, 1, TimeUnit.HOURS);
    }
    
    public void decrementRestaurantLoad(Long restaurantId) {
        String loadKey = RESTAURANT_LOAD_KEY + restaurantId;
        Long currentLoad = redisTemplate.opsForValue().decrement(loadKey);
        
        // Ensure load doesn't go negative
        if (currentLoad != null && currentLoad < 0) {
            redisTemplate.opsForValue().set(loadKey, 0);
        }
    }
    
    public int getQueueLength(Long restaurantId, OrderPriority priority) {
        String queueKey = ORDER_QUEUE_KEY + restaurantId + ":" + priority.name();
        Long count = redisTemplate.opsForZSet().count(queueKey, 0, System.currentTimeMillis());
        return count != null ? count.intValue() : 0;
    }
    
    public int getTotalQueueLength(Long restaurantId) {
        int total = 0;
        for (OrderPriority priority : OrderPriority.values()) {
            total += getQueueLength(restaurantId, priority);
        }
        return total;
    }
    
    public boolean isBurstMode(Long restaurantId) {
        Boolean burstMode = (Boolean) redisTemplate.opsForValue().get("burst_mode:" + restaurantId);
        return burstMode != null && burstMode;
    }
    
    public void clearExpiredQueueEntries() {
        // This would be called by a scheduled job
        // Remove entries older than 30 minutes from all queues
        long cutoffTime = System.currentTimeMillis() - (30 * 60 * 1000);
        
        // Implementation would iterate through all restaurant queues
        // and remove expired entries
    }
    
    private Object createOrderQueueMessage(Order order, OrderPriority priority) {
        return new Object() {
            public final String messageType = "ORDER_QUEUED";
            public final Long orderId = order.getId();
            public final Long restaurantId = order.getRestaurant().getId();
            public final String priority = priority.name();
            public final Long timestamp = System.currentTimeMillis();
        };
    }
    
    private Object createBurstAlert(Long restaurantId, Long orderCount) {
        return new Object() {
            public final String alertType = "BURST_TRAFFIC";
            public final Long restaurantId = restaurantId;
            public final Long orderCount = orderCount;
            public final Long timestamp = System.currentTimeMillis();
            public final String message = "High order volume detected: " + orderCount + " orders/minute";
        };
    }
    
    private Object createOpsAlert(Long restaurantId, Long orderCount) {
        return new Object() {
            public final String alertType = "OPS_BURST_ALERT";
            public final Long restaurantId = restaurantId;
            public final Long orderCount = orderCount;
            public final Long timestamp = System.currentTimeMillis();
            public final String severity = orderCount > 100 ? "CRITICAL" : "HIGH";
        };
    }
}

enum OrderPriority {
    HIGH,    // VIP customers, premium orders
    MEDIUM,  // Regular customers
    LOW      // Promotional orders, first-time users
}