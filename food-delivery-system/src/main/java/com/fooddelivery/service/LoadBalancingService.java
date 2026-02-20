package com.fooddelivery.service;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle load balancing across restaurants and delivery partners
 */
@Service
public class LoadBalancingService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private RestaurantCapacityService capacityService;
    
    @Autowired
    private LocationService locationService;
    
    public List<Restaurant> getAvailableRestaurantsWithLoadBalancing(Double latitude, Double longitude, 
                                                                   String cuisineType, int limit) {
        // Get nearby restaurants
        List<Restaurant> nearbyRestaurants = restaurantRepository.findNearbyRestaurants(latitude, longitude, 15.0);
        
        // Filter by cuisine if specified
        if (cuisineType != null) {
            nearbyRestaurants = nearbyRestaurants.stream()
                    .filter(r -> r.getCuisineType().name().equals(cuisineType))
                    .collect(Collectors.toList());
        }
        
        // Sort by load and capacity
        return nearbyRestaurants.stream()
                .filter(r -> r.getIsActive() && r.getIsAcceptingOrders())
                .filter(r -> capacityService.hasCapacity(r.getId()))
                .sorted((r1, r2) -> {
                    // Primary sort: by load (lower load first)
                    double load1 = getLoadPercentage(r1.getId());
                    double load2 = getLoadPercentage(r2.getId());
                    
                    if (Math.abs(load1 - load2) > 0.1) {
                        return Double.compare(load1, load2);
                    }
                    
                    // Secondary sort: by distance
                    double distance1 = locationService.calculateDistance(latitude, longitude, 
                            r1.getLatitude(), r1.getLongitude());
                    double distance2 = locationService.calculateDistance(latitude, longitude, 
                            r2.getLatitude(), r2.getLongitude());
                    
                    if (Math.abs(distance1 - distance2) > 1.0) {
                        return Double.compare(distance1, distance2);
                    }
                    
                    // Tertiary sort: by rating
                    return r2.getAverageRating().compareTo(r1.getAverageRating());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public Restaurant selectOptimalRestaurant(List<Restaurant> restaurants, Double customerLat, Double customerLng) {
        if (restaurants.isEmpty()) {
            return null;
        }
        
        Restaurant bestRestaurant = null;
        double bestScore = Double.MAX_VALUE;
        
        for (Restaurant restaurant : restaurants) {
            double score = calculateRestaurantScore(restaurant, customerLat, customerLng);
            
            if (score < bestScore) {
                bestScore = score;
                bestRestaurant = restaurant;
            }
        }
        
        return bestRestaurant;
    }
    
    private double calculateRestaurantScore(Restaurant restaurant, Double customerLat, Double customerLng) {
        // Multi-factor scoring algorithm
        double score = 0;
        
        // Factor 1: Current load (30% weight)
        double loadPercentage = getLoadPercentage(restaurant.getId());
        score += loadPercentage * 0.3;
        
        // Factor 2: Distance (25% weight)
        double distance = locationService.calculateDistance(customerLat, customerLng, 
                restaurant.getLatitude(), restaurant.getLongitude());
        score += (distance / 10.0) * 0.25; // Normalize distance
        
        // Factor 3: Estimated delivery time (20% weight)
        int estimatedTime = capacityService.getEstimatedWaitTime(restaurant.getId());
        score += (estimatedTime / 60.0) * 0.2; // Normalize to hours
        
        // Factor 4: Restaurant rating (15% weight) - inverse scoring
        double ratingScore = (5.0 - restaurant.getAverageRating().doubleValue()) / 5.0;
        score += ratingScore * 0.15;
        
        // Factor 5: Delivery fee (10% weight)
        double feeScore = restaurant.getDeliveryFee().doubleValue() / 10.0; // Normalize
        score += feeScore * 0.1;
        
        return score;
    }
    
    public double getLoadPercentage(Long restaurantId) {
        int currentLoad = capacityService.getCurrentLoad(restaurantId);
        int maxCapacity = capacityService.getMaxCapacity(restaurantId);
        
        if (maxCapacity == 0) {
            return 1.0; // 100% if no capacity
        }
        
        return (double) currentLoad / maxCapacity;
    }
    
    public void redistributeLoad() {
        // Get all active restaurants
        List<Restaurant> restaurants = restaurantRepository.findByIsActive(true);
        
        for (Restaurant restaurant : restaurants) {
            double loadPercentage = getLoadPercentage(restaurant.getId());
            
            // If restaurant is overloaded (>90%), try to redirect orders
            if (loadPercentage > 0.9) {
                handleOverloadedRestaurant(restaurant);
            }
            
            // If restaurant is underutilized (<30%), promote it
            if (loadPercentage < 0.3) {
                promoteUnderutilizedRestaurant(restaurant);
            }
        }
    }
    
    private void handleOverloadedRestaurant(Restaurant restaurant) {
        // Temporarily reduce visibility in search results
        String overloadKey = "restaurant_overloaded:" + restaurant.getId();
        redisTemplate.opsForValue().set(overloadKey, true, 30, java.util.concurrent.TimeUnit.MINUTES);
        
        // Find nearby restaurants to redirect orders
        List<Restaurant> alternativeRestaurants = restaurantRepository.findNearbyRestaurants(
                restaurant.getLatitude(), restaurant.getLongitude(), 5.0);
        
        alternativeRestaurants = alternativeRestaurants.stream()
                .filter(r -> !r.getId().equals(restaurant.getId()))
                .filter(r -> r.getCuisineType().equals(restaurant.getCuisineType()))
                .filter(r -> capacityService.hasCapacity(r.getId()))
                .collect(Collectors.toList());
        
        // Store alternative restaurants for redirection
        if (!alternativeRestaurants.isEmpty()) {
            String alternativesKey = "restaurant_alternatives:" + restaurant.getId();
            redisTemplate.opsForList().rightPushAll(alternativesKey, 
                    alternativeRestaurants.stream().map(r -> r.getId()).toArray());
            redisTemplate.expire(alternativesKey, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
    }
    
    private void promoteUnderutilizedRestaurant(Restaurant restaurant) {
        // Boost restaurant in search results
        String boostKey = "restaurant_boost:" + restaurant.getId();
        redisTemplate.opsForValue().set(boostKey, true, 60, java.util.concurrent.TimeUnit.MINUTES);
        
        // Could also trigger promotional campaigns
    }
    
    public boolean isRestaurantOverloaded(Long restaurantId) {
        String overloadKey = "restaurant_overloaded:" + restaurantId;
        Boolean overloaded = (Boolean) redisTemplate.opsForValue().get(overloadKey);
        return overloaded != null && overloaded;
    }
    
    public boolean isRestaurantBoosted(Long restaurantId) {
        String boostKey = "restaurant_boost:" + restaurantId;
        Boolean boosted = (Boolean) redisTemplate.opsForValue().get(boostKey);
        return boosted != null && boosted;
    }
    
    public List<Long> getAlternativeRestaurants(Long restaurantId) {
        String alternativesKey = "restaurant_alternatives:" + restaurantId;
        return redisTemplate.opsForList().range(alternativesKey, 0, -1)
                .stream()
                .map(obj -> (Long) obj)
                .collect(Collectors.toList());
    }
    
    public void updateRestaurantPriority(Long restaurantId, double priority) {
        String priorityKey = "restaurant_priority:" + restaurantId;
        redisTemplate.opsForValue().set(priorityKey, priority, 1, java.util.concurrent.TimeUnit.HOURS);
    }
    
    public double getRestaurantPriority(Long restaurantId) {
        String priorityKey = "restaurant_priority:" + restaurantId;
        Double priority = (Double) redisTemplate.opsForValue().get(priorityKey);
        return priority != null ? priority : 1.0; // Default priority
    }
}