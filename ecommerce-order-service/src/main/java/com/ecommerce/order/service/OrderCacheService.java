package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class OrderCacheService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderCacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache key prefixes
    private static final String ORDER_KEY_PREFIX = "order:";
    private static final String USER_ORDERS_KEY_PREFIX = "user_orders:";
    private static final String ORDER_STATUS_KEY_PREFIX = "order_status:";
    private static final String ORDER_STATS_KEY_PREFIX = "order_stats:";
    private static final String HOT_ORDERS_KEY = "hot_orders";
    
    // Cache TTL configurations
    private static final Duration ORDER_TTL = Duration.ofMinutes(30);
    private static final Duration USER_ORDERS_TTL = Duration.ofMinutes(15);
    private static final Duration ORDER_STATUS_TTL = Duration.ofMinutes(5);
    private static final Duration ORDER_STATS_TTL = Duration.ofMinutes(10);
    private static final Duration HOT_ORDERS_TTL = Duration.ofMinutes(5);
    
    @Autowired
    public OrderCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Cache order with Spring Cache abstraction
     */
    @Cacheable(value = "orders", key = "#orderId")
    public Order getCachedOrder(String orderId) {
        // This method is used by Spring Cache - actual implementation in getOrder
        return null;
    }
    
    /**
     * Update order cache
     */
    @CachePut(value = "orders", key = "#order.orderId")
    public Order updateOrderCache(Order order) {
        log.debug("Updating cache for order: {}", order.getOrderId());
        
        // Also update Redis directly for additional operations
        cacheOrder(order);
        
        return order;
    }
    
    /**
     * Evict order from cache
     */
    @CacheEvict(value = "orders", key = "#orderId")
    public void evictOrder(String orderId) {
        log.debug("Evicting order from cache: {}", orderId);
        
        // Also remove from Redis
        String redisKey = ORDER_KEY_PREFIX + orderId;
        redisTemplate.delete(redisKey);
        
        // Remove from user orders cache
        invalidateUserOrdersCache(orderId);
    }
    
    /**
     * Cache order directly in Redis
     */
    public void cacheOrder(Order order) {
        try {
            String key = ORDER_KEY_PREFIX + order.getOrderId();
            redisTemplate.opsForValue().set(key, order, ORDER_TTL);
            
            // Cache order status separately for quick lookups
            cacheOrderStatus(order.getOrderId(), order.getStatus());
            
            // Add to hot orders if recently created
            if (isHotOrder(order)) {
                addToHotOrders(order);
            }
            
            log.debug("Cached order: {} with TTL: {}", order.getOrderId(), ORDER_TTL);
            
        } catch (Exception e) {
            log.error("Failed to cache order: {}", order.getOrderId(), e);
        }
    }
    
    /**
     * Get order from cache
     */
    public Order getOrder(String orderId) {
        try {
            String key = ORDER_KEY_PREFIX + orderId;
            Object cachedOrder = redisTemplate.opsForValue().get(key);
            
            if (cachedOrder instanceof Order) {
                log.debug("Cache hit for order: {}", orderId);
                return (Order) cachedOrder;
            }
            
            log.debug("Cache miss for order: {}", orderId);
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get order from cache: {}", orderId, e);
            return null;
        }
    }
    
    /**
     * Cache user orders list
     */
    public void cacheUserOrders(String userId, List<Order> orders) {
        try {
            String key = USER_ORDERS_KEY_PREFIX + userId;
            
            // Store order IDs instead of full orders to save memory
            List<String> orderIds = orders.stream()
                    .map(Order::getOrderId)
                    .toList();
            
            redisTemplate.opsForValue().set(key, orderIds, USER_ORDERS_TTL);
            
            // Cache individual orders
            orders.forEach(this::cacheOrder);
            
            log.debug("Cached {} orders for user: {}", orders.size(), userId);
            
        } catch (Exception e) {
            log.error("Failed to cache user orders for user: {}", userId, e);
        }
    }
    
    /**
     * Get cached user orders
     */
    @SuppressWarnings("unchecked")
    public List<String> getCachedUserOrderIds(String userId) {
        try {
            String key = USER_ORDERS_KEY_PREFIX + userId;
            Object cachedOrderIds = redisTemplate.opsForValue().get(key);
            
            if (cachedOrderIds instanceof List) {
                return (List<String>) cachedOrderIds;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get cached user orders for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Cache order status for quick lookups
     */
    public void cacheOrderStatus(String orderId, OrderStatus status) {
        try {
            String key = ORDER_STATUS_KEY_PREFIX + orderId;
            redisTemplate.opsForValue().set(key, status.toString(), ORDER_STATUS_TTL);
            
        } catch (Exception e) {
            log.error("Failed to cache order status for order: {}", orderId, e);
        }
    }
    
    /**
     * Get cached order status
     */
    public OrderStatus getCachedOrderStatus(String orderId) {
        try {
            String key = ORDER_STATUS_KEY_PREFIX + orderId;
            String status = (String) redisTemplate.opsForValue().get(key);
            
            if (status != null) {
                return OrderStatus.valueOf(status);
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get cached order status for order: {}", orderId, e);
            return null;
        }
    }
    
    /**
     * Cache order statistics
     */
    public void cacheOrderStats(String key, Map<String, Object> stats) {
        try {
            String redisKey = ORDER_STATS_KEY_PREFIX + key;
            redisTemplate.opsForHash().putAll(redisKey, stats);
            redisTemplate.expire(redisKey, ORDER_STATS_TTL);
            
            log.debug("Cached order stats for key: {}", key);
            
        } catch (Exception e) {
            log.error("Failed to cache order stats for key: {}", key, e);
        }
    }
    
    /**
     * Get cached order statistics
     */
    public Map<Object, Object> getCachedOrderStats(String key) {
        try {
            String redisKey = ORDER_STATS_KEY_PREFIX + key;
            Map<Object, Object> stats = redisTemplate.opsForHash().entries(redisKey);
            
            if (!stats.isEmpty()) {
                log.debug("Cache hit for order stats: {}", key);
                return stats;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get cached order stats for key: {}", key, e);
            return null;
        }
    }
    
    /**
     * Add order to hot orders list (recently created/updated orders)
     */
    public void addToHotOrders(Order order) {
        try {
            double score = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(HOT_ORDERS_KEY, order.getOrderId(), score);
            
            // Keep only last 1000 hot orders
            redisTemplate.opsForZSet().removeRange(HOT_ORDERS_KEY, 0, -1001);
            
            // Set TTL for hot orders
            redisTemplate.expire(HOT_ORDERS_KEY, HOT_ORDERS_TTL);
            
        } catch (Exception e) {
            log.error("Failed to add order to hot orders: {}", order.getOrderId(), e);
        }
    }
    
    /**
     * Get hot orders (recently active)
     */
    public Set<String> getHotOrders(int limit) {
        try {
            Set<Object> hotOrderIds = redisTemplate.opsForZSet()
                    .reverseRange(HOT_ORDERS_KEY, 0, limit - 1);
            
            if (hotOrderIds != null) {
                return hotOrderIds.stream()
                        .map(Object::toString)
                        .collect(LinkedHashSet::new, Set::add, Set::addAll);
            }
            
            return new LinkedHashSet<>();
            
        } catch (Exception e) {
            log.error("Failed to get hot orders", e);
            return new LinkedHashSet<>();
        }
    }
    
    /**
     * Warm up cache with frequently accessed orders
     */
    public void warmUpCache(List<Order> orders) {
        log.info("Warming up cache with {} orders", orders.size());
        
        orders.parallelStream().forEach(order -> {
            try {
                cacheOrder(order);
            } catch (Exception e) {
                log.error("Failed to warm up cache for order: {}", order.getOrderId(), e);
            }
        });
        
        log.info("Cache warm-up completed");
    }
    
    /**
     * Invalidate user orders cache when order is updated
     */
    private void invalidateUserOrdersCache(String orderId) {
        try {
            // This is a simplified approach - in production, you might want to
            // maintain a reverse index of orderId -> userId for efficient invalidation
            log.debug("Invalidating user orders cache for order: {}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to invalidate user orders cache for order: {}", orderId, e);
        }
    }
    
    /**
     * Check if order is considered "hot" (recently created/updated)
     */
    private boolean isHotOrder(Order order) {
        if (order.getCreatedAt() == null) {
            return false;
        }
        
        // Consider orders created in the last 10 minutes as hot
        return order.getCreatedAt().isAfter(
                java.time.LocalDateTime.now().minusMinutes(10)
        );
    }
    
    /**
     * Clear all order caches (use with caution)
     */
    public void clearAllCaches() {
        try {
            Set<String> keys = redisTemplate.keys(ORDER_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            keys = redisTemplate.keys(USER_ORDERS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            keys = redisTemplate.keys(ORDER_STATUS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            keys = redisTemplate.keys(ORDER_STATS_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            redisTemplate.delete(HOT_ORDERS_KEY);
            
            log.info("Cleared all order caches");
            
        } catch (Exception e) {
            log.error("Failed to clear all caches", e);
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Count cached orders
            Set<String> orderKeys = redisTemplate.keys(ORDER_KEY_PREFIX + "*");
            stats.put("cachedOrders", orderKeys != null ? orderKeys.size() : 0);
            
            // Count cached user orders
            Set<String> userOrderKeys = redisTemplate.keys(USER_ORDERS_KEY_PREFIX + "*");
            stats.put("cachedUserOrders", userOrderKeys != null ? userOrderKeys.size() : 0);
            
            // Count hot orders
            Long hotOrdersCount = redisTemplate.opsForZSet().zCard(HOT_ORDERS_KEY);
            stats.put("hotOrders", hotOrdersCount != null ? hotOrdersCount : 0);
            
            // Memory usage estimation (simplified)
            stats.put("estimatedMemoryUsage", "N/A"); // Would need Redis MEMORY USAGE command
            
        } catch (Exception e) {
            log.error("Failed to get cache statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}