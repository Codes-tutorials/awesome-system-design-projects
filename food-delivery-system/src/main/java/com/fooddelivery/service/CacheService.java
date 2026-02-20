package com.fooddelivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Advanced caching service for high-performance data access
 */
@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Cache key prefixes
    private static final String RESTAURANT_PREFIX = "restaurant:";
    private static final String MENU_PREFIX = "menu:";
    private static final String USER_PREFIX = "user:";
    private static final String ORDER_PREFIX = "order:";
    private static final String SEARCH_PREFIX = "search:";
    private static final String LOCATION_PREFIX = "location:";
    
    // Cache TTL configurations
    private static final int RESTAURANT_TTL = 30; // 30 minutes
    private static final int MENU_TTL = 60; // 1 hour
    private static final int USER_TTL = 15; // 15 minutes
    private static final int SEARCH_TTL = 5; // 5 minutes
    private static final int LOCATION_TTL = 10; // 10 minutes
    
    public void cacheRestaurant(Long restaurantId, Object restaurant) {
        String key = RESTAURANT_PREFIX + restaurantId;
        redisTemplate.opsForValue().set(key, restaurant, RESTAURANT_TTL, TimeUnit.MINUTES);
    }
    
    public <T> T getRestaurant(Long restaurantId, Class<T> type) {
        String key = RESTAURANT_PREFIX + restaurantId;
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToType(cached, type);
    }
    
    public void cacheMenu(Long restaurantId, Object menu) {
        String key = MENU_PREFIX + restaurantId;
        redisTemplate.opsForValue().set(key, menu, MENU_TTL, TimeUnit.MINUTES);
    }
    
    public <T> T getMenu(Long restaurantId, Class<T> type) {
        String key = MENU_PREFIX + restaurantId;
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToType(cached, type);
    }
    
    public void cacheUser(Long userId, Object user) {
        String key = USER_PREFIX + userId;
        redisTemplate.opsForValue().set(key, user, USER_TTL, TimeUnit.MINUTES);
    }
    
    public <T> T getUser(Long userId, Class<T> type) {
        String key = USER_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToType(cached, type);
    }
    
    public void cacheSearchResults(String searchQuery, Object results) {
        String key = SEARCH_PREFIX + searchQuery.hashCode();
        redisTemplate.opsForValue().set(key, results, SEARCH_TTL, TimeUnit.MINUTES);
    }
    
    public <T> T getSearchResults(String searchQuery, Class<T> type) {
        String key = SEARCH_PREFIX + searchQuery.hashCode();
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToType(cached, type);
    }
    
    public void cacheLocationData(String locationKey, Object locationData) {
        String key = LOCATION_PREFIX + locationKey;
        redisTemplate.opsForValue().set(key, locationData, LOCATION_TTL, TimeUnit.MINUTES);
    }
    
    public <T> T getLocationData(String locationKey, Class<T> type) {
        String key = LOCATION_PREFIX + locationKey;
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToType(cached, type);
    }
    
    // Hot data caching for frequently accessed items
    public void cacheHotRestaurants(List<Object> restaurants) {
        String key = "hot_restaurants";
        redisTemplate.opsForValue().set(key, restaurants, 5, TimeUnit.MINUTES);
    }
    
    public <T> List<T> getHotRestaurants(Class<T> type) {
        String key = "hot_restaurants";
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToList(cached, type);
    }
    
    public void cacheTrendingItems(List<Object> items) {
        String key = "trending_items";
        redisTemplate.opsForValue().set(key, items, 10, TimeUnit.MINUTES);
    }
    
    public <T> List<T> getTrendingItems(Class<T> type) {
        String key = "trending_items";
        Object cached = redisTemplate.opsForValue().get(key);
        return convertToList(cached, type);
    }
    
    // Geospatial caching for location-based queries
    public void addRestaurantLocation(Long restaurantId, double latitude, double longitude) {
        String key = "restaurant_locations";
        redisTemplate.opsForGeo().add(key, new org.springframework.data.geo.Point(longitude, latitude), 
                                     restaurantId.toString());
    }
    
    public List<String> findNearbyRestaurants(double latitude, double longitude, double radiusKm) {
        String key = "restaurant_locations";
        org.springframework.data.geo.Circle circle = new org.springframework.data.geo.Circle(
                new org.springframework.data.geo.Point(longitude, latitude), 
                new org.springframework.data.geo.Distance(radiusKm, org.springframework.data.geo.Metrics.KILOMETERS));
        
        return redisTemplate.opsForGeo().radius(key, circle)
                .getContent()
                .stream()
                .map(result -> result.getContent().getName())
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Cache warming strategies
    public void warmRestaurantCache(List<Long> restaurantIds) {
        // This would be called during low-traffic periods
        for (Long restaurantId : restaurantIds) {
            // Fetch and cache restaurant data
            // Implementation would call actual service methods
        }
    }
    
    public void warmMenuCache(List<Long> restaurantIds) {
        for (Long restaurantId : restaurantIds) {
            // Fetch and cache menu data
            // Implementation would call actual service methods
        }
    }
    
    // Cache invalidation
    public void invalidateRestaurant(Long restaurantId) {
        String key = RESTAURANT_PREFIX + restaurantId;
        redisTemplate.delete(key);
        
        // Also invalidate related caches
        invalidateMenu(restaurantId);
        invalidateSearchCache();
    }
    
    public void invalidateMenu(Long restaurantId) {
        String key = MENU_PREFIX + restaurantId;
        redisTemplate.delete(key);
    }
    
    public void invalidateUser(Long userId) {
        String key = USER_PREFIX + userId;
        redisTemplate.delete(key);
    }
    
    public void invalidateSearchCache() {
        Set<String> keys = redisTemplate.keys(SEARCH_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    // Bulk operations for performance
    public void cacheMultipleRestaurants(java.util.Map<Long, Object> restaurants) {
        restaurants.forEach((id, restaurant) -> {
            String key = RESTAURANT_PREFIX + id;
            redisTemplate.opsForValue().set(key, restaurant, RESTAURANT_TTL, TimeUnit.MINUTES);
        });
    }
    
    public java.util.Map<Long, Object> getMultipleRestaurants(List<Long> restaurantIds) {
        List<String> keys = restaurantIds.stream()
                .map(id -> RESTAURANT_PREFIX + id)
                .collect(java.util.stream.Collectors.toList());
        
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        
        java.util.Map<Long, Object> result = new java.util.HashMap<>();
        for (int i = 0; i < restaurantIds.size(); i++) {
            if (values.get(i) != null) {
                result.put(restaurantIds.get(i), values.get(i));
            }
        }
        
        return result;
    }
    
    // Cache statistics and monitoring
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();
        
        // Count keys by prefix
        stats.setRestaurantCount(countKeysByPrefix(RESTAURANT_PREFIX));
        stats.setMenuCount(countKeysByPrefix(MENU_PREFIX));
        stats.setUserCount(countKeysByPrefix(USER_PREFIX));
        stats.setSearchCount(countKeysByPrefix(SEARCH_PREFIX));
        
        return stats;
    }
    
    private long countKeysByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys != null ? keys.size() : 0;
    }
    
    // Utility methods
    @SuppressWarnings("unchecked")
    private <T> T convertToType(Object cached, Class<T> type) {
        if (cached == null) {
            return null;
        }
        
        try {
            if (type.isInstance(cached)) {
                return (T) cached;
            }
            
            // Convert using ObjectMapper if needed
            return objectMapper.convertValue(cached, type);
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> convertToList(Object cached, Class<T> type) {
        if (cached == null) {
            return null;
        }
        
        try {
            if (cached instanceof List) {
                return (List<T>) cached;
            }
            
            // Convert using ObjectMapper if needed
            return objectMapper.convertValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            return null;
        }
    }
    
    // Cache statistics DTO
    public static class CacheStats {
        private long restaurantCount;
        private long menuCount;
        private long userCount;
        private long searchCount;
        
        // Getters and setters
        public long getRestaurantCount() { return restaurantCount; }
        public void setRestaurantCount(long restaurantCount) { this.restaurantCount = restaurantCount; }
        
        public long getMenuCount() { return menuCount; }
        public void setMenuCount(long menuCount) { this.menuCount = menuCount; }
        
        public long getUserCount() { return userCount; }
        public void setUserCount(long userCount) { this.userCount = userCount; }
        
        public long getSearchCount() { return searchCount; }
        public void setSearchCount(long searchCount) { this.searchCount = searchCount; }
    }
}