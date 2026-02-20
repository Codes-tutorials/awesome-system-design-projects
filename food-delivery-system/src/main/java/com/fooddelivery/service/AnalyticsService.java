package com.fooddelivery.service;

import com.fooddelivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for real-time analytics and monitoring
 */
@Service
public class AnalyticsService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String METRICS_PREFIX = "metrics:";
    private static final String ALERTS_PREFIX = "alerts:";
    
    public void trackOrderPlaced(Long restaurantId, BigDecimal orderValue) {
        String timeWindow = getCurrentTimeWindow();
        
        // Track orders per minute
        String orderCountKey = METRICS_PREFIX + "orders:" + timeWindow;
        redisTemplate.opsForValue().increment(orderCountKey);
        redisTemplate.expire(orderCountKey, 60, TimeUnit.MINUTES);
        
        // Track revenue per minute
        String revenueKey = METRICS_PREFIX + "revenue:" + timeWindow;
        redisTemplate.opsForValue().increment(revenueKey, orderValue.doubleValue());
        redisTemplate.expire(revenueKey, 60, TimeUnit.MINUTES);
        
        // Track restaurant-specific metrics
        String restaurantOrderKey = METRICS_PREFIX + "restaurant:" + restaurantId + ":orders:" + timeWindow;
        redisTemplate.opsForValue().increment(restaurantOrderKey);
        redisTemplate.expire(restaurantOrderKey, 60, TimeUnit.MINUTES);
        
        // Check for anomalies
        checkOrderAnomalies(timeWindow);
        
        // Send to analytics pipeline
        kafkaTemplate.send("analytics-events", createOrderEvent(restaurantId, orderValue));
    }
    
    public void trackDeliveryCompleted(Long deliveryPartnerId, int deliveryTimeMinutes) {
        String timeWindow = getCurrentTimeWindow();
        
        // Track deliveries per minute
        String deliveryCountKey = METRICS_PREFIX + "deliveries:" + timeWindow;
        redisTemplate.opsForValue().increment(deliveryCountKey);
        redisTemplate.expire(deliveryCountKey, 60, TimeUnit.MINUTES);
        
        // Track average delivery time
        String deliveryTimeKey = METRICS_PREFIX + "delivery_time:" + timeWindow;
        updateAverageMetric(deliveryTimeKey, deliveryTimeMinutes);
        
        // Track partner-specific metrics
        String partnerDeliveryKey = METRICS_PREFIX + "partner:" + deliveryPartnerId + ":deliveries:" + timeWindow;
        redisTemplate.opsForValue().increment(partnerDeliveryKey);
        redisTemplate.expire(partnerDeliveryKey, 60, TimeUnit.MINUTES);
        
        // Check delivery performance
        checkDeliveryPerformance(deliveryPartnerId, deliveryTimeMinutes);
        
        kafkaTemplate.send("analytics-events", createDeliveryEvent(deliveryPartnerId, deliveryTimeMinutes));
    }
    
    public void trackUserActivity(Long userId, String activity) {
        String timeWindow = getCurrentTimeWindow();
        
        // Track user activity
        String activityKey = METRICS_PREFIX + "activity:" + activity + ":" + timeWindow;
        redisTemplate.opsForValue().increment(activityKey);
        redisTemplate.expire(activityKey, 60, TimeUnit.MINUTES);
        
        // Track user-specific activity
        String userActivityKey = METRICS_PREFIX + "user:" + userId + ":activity:" + timeWindow;
        redisTemplate.opsForHash().increment(userActivityKey, activity, 1);
        redisTemplate.expire(userActivityKey, 24, TimeUnit.HOURS);
        
        kafkaTemplate.send("user-analytics", createUserActivityEvent(userId, activity));
    }
    
    public SystemMetrics getCurrentSystemMetrics() {
        String timeWindow = getCurrentTimeWindow();
        
        SystemMetrics metrics = new SystemMetrics();
        
        // Get current orders per minute
        String orderCountKey = METRICS_PREFIX + "orders:" + timeWindow;
        Integer ordersPerMinute = (Integer) redisTemplate.opsForValue().get(orderCountKey);
        metrics.setOrdersPerMinute(ordersPerMinute != null ? ordersPerMinute : 0);
        
        // Get current revenue per minute
        String revenueKey = METRICS_PREFIX + "revenue:" + timeWindow;
        Double revenuePerMinute = (Double) redisTemplate.opsForValue().get(revenueKey);
        metrics.setRevenuePerMinute(revenuePerMinute != null ? BigDecimal.valueOf(revenuePerMinute) : BigDecimal.ZERO);
        
        // Get active restaurants
        Long activeRestaurants = restaurantRepository.countAvailableRestaurants();
        metrics.setActiveRestaurants(activeRestaurants);
        
        // Get online delivery partners
        Long onlinePartners = deliveryPartnerRepository.countOnlinePartners();
        metrics.setOnlineDeliveryPartners(onlinePartners);
        
        // Get system load
        metrics.setSystemLoad(calculateSystemLoad());
        
        return metrics;
    }
    
    public RestaurantMetrics getRestaurantMetrics(Long restaurantId) {
        String timeWindow = getCurrentTimeWindow();
        
        RestaurantMetrics metrics = new RestaurantMetrics();
        metrics.setRestaurantId(restaurantId);
        
        // Orders in current window
        String orderKey = METRICS_PREFIX + "restaurant:" + restaurantId + ":orders:" + timeWindow;
        Integer orders = (Integer) redisTemplate.opsForValue().get(orderKey);
        metrics.setOrdersThisHour(orders != null ? orders : 0);
        
        // Average preparation time
        String prepTimeKey = METRICS_PREFIX + "restaurant:" + restaurantId + ":prep_time";
        Double avgPrepTime = (Double) redisTemplate.opsForValue().get(prepTimeKey);
        metrics.setAveragePreparationTime(avgPrepTime != null ? avgPrepTime : 0.0);
        
        // Current load percentage
        metrics.setLoadPercentage(calculateRestaurantLoad(restaurantId));
        
        // Queue length
        metrics.setQueueLength(getRestaurantQueueLength(restaurantId));
        
        return metrics;
    }
    
    public void detectAnomalies() {
        String timeWindow = getCurrentTimeWindow();
        
        // Check order volume anomalies
        String orderCountKey = METRICS_PREFIX + "orders:" + timeWindow;
        Integer currentOrders = (Integer) redisTemplate.opsForValue().get(orderCountKey);
        
        if (currentOrders != null) {
            double avgOrders = getHistoricalAverage("orders", 60); // Last 60 minutes
            
            if (currentOrders > avgOrders * 2) {
                createAlert("HIGH_ORDER_VOLUME", "Order volume is " + currentOrders + " vs average " + avgOrders);
            }
            
            if (currentOrders < avgOrders * 0.3 && avgOrders > 10) {
                createAlert("LOW_ORDER_VOLUME", "Order volume is unusually low: " + currentOrders);
            }
        }
        
        // Check delivery time anomalies
        checkDeliveryTimeAnomalies();
        
        // Check system performance
        checkSystemPerformance();
    }
    
    private void checkOrderAnomalies(String timeWindow) {
        String orderCountKey = METRICS_PREFIX + "orders:" + timeWindow;
        Integer currentOrders = (Integer) redisTemplate.opsForValue().get(orderCountKey);
        
        if (currentOrders != null && currentOrders > 100) { // Threshold for burst
            createAlert("ORDER_BURST", "High order volume detected: " + currentOrders + " orders/minute");
        }
    }
    
    private void checkDeliveryPerformance(Long deliveryPartnerId, int deliveryTimeMinutes) {
        if (deliveryTimeMinutes > 60) { // More than 1 hour
            createAlert("SLOW_DELIVERY", "Delivery partner " + deliveryPartnerId + 
                       " took " + deliveryTimeMinutes + " minutes for delivery");
        }
    }
    
    private void checkDeliveryTimeAnomalies() {
        String timeWindow = getCurrentTimeWindow();
        String deliveryTimeKey = METRICS_PREFIX + "delivery_time:" + timeWindow;
        
        Double currentAvgTime = (Double) redisTemplate.opsForValue().get(deliveryTimeKey);
        if (currentAvgTime != null) {
            double historicalAvg = getHistoricalAverage("delivery_time", 60);
            
            if (currentAvgTime > historicalAvg * 1.5) {
                createAlert("HIGH_DELIVERY_TIME", "Average delivery time is " + currentAvgTime + 
                           " vs historical average " + historicalAvg);
            }
        }
    }
    
    private void checkSystemPerformance() {
        double systemLoad = calculateSystemLoad();
        
        if (systemLoad > 0.8) {
            createAlert("HIGH_SYSTEM_LOAD", "System load is at " + (systemLoad * 100) + "%");
        }
    }
    
    private void createAlert(String alertType, String message) {
        String alertKey = ALERTS_PREFIX + alertType + ":" + System.currentTimeMillis();
        
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", alertType);
        alert.put("message", message);
        alert.put("timestamp", System.currentTimeMillis());
        alert.put("severity", determineAlertSeverity(alertType));
        
        redisTemplate.opsForValue().set(alertKey, alert, 24, TimeUnit.HOURS);
        
        // Send to alerting system
        kafkaTemplate.send("system-alerts", alert);
    }
    
    private String determineAlertSeverity(String alertType) {
        switch (alertType) {
            case "HIGH_SYSTEM_LOAD":
            case "ORDER_BURST":
                return "HIGH";
            case "HIGH_DELIVERY_TIME":
            case "SLOW_DELIVERY":
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
    
    private double getHistoricalAverage(String metric, int windowMinutes) {
        // Calculate average from historical data
        // This is a simplified implementation
        double sum = 0;
        int count = 0;
        
        long currentTime = System.currentTimeMillis();
        for (int i = 1; i <= windowMinutes; i++) {
            long pastTime = currentTime - (i * 60 * 1000);
            String pastWindow = String.valueOf(pastTime / 60000);
            String key = METRICS_PREFIX + metric + ":" + pastWindow;
            
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                if (value instanceof Integer) {
                    sum += (Integer) value;
                } else if (value instanceof Double) {
                    sum += (Double) value;
                }
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    private double calculateSystemLoad() {
        // Calculate system load based on various factors
        long totalOrders = orderRepository.countOrdersByStatus(com.fooddelivery.model.OrderStatus.PLACED);
        long activeRestaurants = restaurantRepository.countAvailableRestaurants();
        long onlinePartners = deliveryPartnerRepository.countOnlinePartners();
        
        // Simple load calculation (can be made more sophisticated)
        double orderLoad = Math.min(totalOrders / 100.0, 1.0);
        double capacityLoad = activeRestaurants > 0 ? Math.min(totalOrders / (activeRestaurants * 10.0), 1.0) : 1.0;
        double partnerLoad = onlinePartners > 0 ? Math.min(totalOrders / (onlinePartners * 5.0), 1.0) : 1.0;
        
        return (orderLoad + capacityLoad + partnerLoad) / 3.0;
    }
    
    private double calculateRestaurantLoad(Long restaurantId) {
        // This would integrate with RestaurantCapacityService
        return 0.5; // Placeholder
    }
    
    private int getRestaurantQueueLength(Long restaurantId) {
        // This would integrate with OrderQueueService
        return 0; // Placeholder
    }
    
    private void updateAverageMetric(String key, double newValue) {
        // Update running average
        Map<Object, Object> avgData = redisTemplate.opsForHash().entries(key);
        
        double currentAvg = 0;
        int count = 0;
        
        if (avgData.containsKey("avg")) {
            currentAvg = (Double) avgData.get("avg");
            count = (Integer) avgData.get("count");
        }
        
        count++;
        double newAvg = ((currentAvg * (count - 1)) + newValue) / count;
        
        redisTemplate.opsForHash().put(key, "avg", newAvg);
        redisTemplate.opsForHash().put(key, "count", count);
        redisTemplate.expire(key, 60, TimeUnit.MINUTES);
    }
    
    private String getCurrentTimeWindow() {
        return String.valueOf(System.currentTimeMillis() / 60000); // 1-minute windows
    }
    
    private Object createOrderEvent(Long restaurantId, BigDecimal orderValue) {
        return new Object() {
            public final String eventType = "ORDER_PLACED";
            public final Long restaurantId = restaurantId;
            public final BigDecimal orderValue = orderValue;
            public final Long timestamp = System.currentTimeMillis();
        };
    }
    
    private Object createDeliveryEvent(Long deliveryPartnerId, int deliveryTimeMinutes) {
        return new Object() {
            public final String eventType = "DELIVERY_COMPLETED";
            public final Long deliveryPartnerId = deliveryPartnerId;
            public final int deliveryTimeMinutes = deliveryTimeMinutes;
            public final Long timestamp = System.currentTimeMillis();
        };
    }
    
    private Object createUserActivityEvent(Long userId, String activity) {
        return new Object() {
            public final String eventType = "USER_ACTIVITY";
            public final Long userId = userId;
            public final String activity = activity;
            public final Long timestamp = System.currentTimeMillis();
        };
    }
    
    // DTO classes
    public static class SystemMetrics {
        private int ordersPerMinute;
        private BigDecimal revenuePerMinute;
        private Long activeRestaurants;
        private Long onlineDeliveryPartners;
        private double systemLoad;
        
        // Getters and setters
        public int getOrdersPerMinute() { return ordersPerMinute; }
        public void setOrdersPerMinute(int ordersPerMinute) { this.ordersPerMinute = ordersPerMinute; }
        
        public BigDecimal getRevenuePerMinute() { return revenuePerMinute; }
        public void setRevenuePerMinute(BigDecimal revenuePerMinute) { this.revenuePerMinute = revenuePerMinute; }
        
        public Long getActiveRestaurants() { return activeRestaurants; }
        public void setActiveRestaurants(Long activeRestaurants) { this.activeRestaurants = activeRestaurants; }
        
        public Long getOnlineDeliveryPartners() { return onlineDeliveryPartners; }
        public void setOnlineDeliveryPartners(Long onlineDeliveryPartners) { this.onlineDeliveryPartners = onlineDeliveryPartners; }
        
        public double getSystemLoad() { return systemLoad; }
        public void setSystemLoad(double systemLoad) { this.systemLoad = systemLoad; }
    }
    
    public static class RestaurantMetrics {
        private Long restaurantId;
        private int ordersThisHour;
        private double averagePreparationTime;
        private double loadPercentage;
        private int queueLength;
        
        // Getters and setters
        public Long getRestaurantId() { return restaurantId; }
        public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
        
        public int getOrdersThisHour() { return ordersThisHour; }
        public void setOrdersThisHour(int ordersThisHour) { this.ordersThisHour = ordersThisHour; }
        
        public double getAveragePreparationTime() { return averagePreparationTime; }
        public void setAveragePreparationTime(double averagePreparationTime) { this.averagePreparationTime = averagePreparationTime; }
        
        public double getLoadPercentage() { return loadPercentage; }
        public void setLoadPercentage(double loadPercentage) { this.loadPercentage = loadPercentage; }
        
        public int getQueueLength() { return queueLength; }
        public void setQueueLength(int queueLength) { this.queueLength = queueLength; }
    }
}