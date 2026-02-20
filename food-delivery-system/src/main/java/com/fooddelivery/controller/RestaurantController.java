package com.fooddelivery.controller;

import com.fooddelivery.model.Restaurant;
import com.fooddelivery.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Restaurant management operations
 */
@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurant Management", description = "APIs for restaurant operations, search, and management")
public class RestaurantController {
    
    @Autowired
    private RestaurantService restaurantService;
    
    @PostMapping
    @Operation(summary = "Create new restaurant", description = "Register a new restaurant in the system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Restaurant> createRestaurant(@Valid @RequestBody Restaurant restaurant) {
        try {
            Restaurant savedRestaurant = restaurantService.createRestaurant(restaurant);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRestaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Retrieve restaurant details by ID")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return restaurantService.findById(id)
                .map(restaurant -> ResponseEntity.ok(restaurant))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Retrieve all restaurants with pagination")
    public ResponseEntity<Page<Restaurant>> getAllRestaurants(Pageable pageable) {
        Page<Restaurant> restaurants = restaurantService.findAll(pageable);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active restaurants", description = "Retrieve all active restaurants")
    public ResponseEntity<List<Restaurant>> getActiveRestaurants() {
        List<Restaurant> restaurants = restaurantService.findActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Search restaurants by name, cuisine, or location")
    public ResponseEntity<List<Restaurant>> searchRestaurants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        
        List<Restaurant> restaurants;
        
        if (latitude != null && longitude != null) {
            restaurants = restaurantService.findNearbyRestaurants(latitude, longitude, radiusKm);
        } else if (name != null || cuisine != null || location != null) {
            restaurants = restaurantService.searchRestaurants(name, cuisine, location);
        } else {
            restaurants = restaurantService.findActiveRestaurants();
        }
        
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby restaurants", description = "Find restaurants within specified radius")
    public ResponseEntity<List<Restaurant>> getNearbyRestaurants(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        
        List<Restaurant> restaurants = restaurantService.findNearbyRestaurants(latitude, longitude, radiusKm);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/cuisine/{cuisine}")
    @Operation(summary = "Get restaurants by cuisine", description = "Find restaurants serving specific cuisine")
    public ResponseEntity<List<Restaurant>> getRestaurantsByCuisine(@PathVariable String cuisine) {
        List<Restaurant> restaurants = restaurantService.findByCuisineType(cuisine);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated restaurants", description = "Retrieve restaurants with highest ratings")
    public ResponseEntity<List<Restaurant>> getTopRatedRestaurants(
            @RequestParam(defaultValue = "10") int limit) {
        List<Restaurant> restaurants = restaurantService.findTopRatedRestaurants(limit);
        return ResponseEntity.ok(restaurants);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular restaurants", description = "Retrieve most popular restaurants based on order count")
    public ResponseEntity<List<Restaurant>> getPopularRestaurants(
            @RequestParam(defaultValue = "10") int limit) {
        List<Restaurant> restaurants = restaurantService.findPopularRestaurants(limit);
        return ResponseEntity.ok(restaurants);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant", description = "Update restaurant information")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Restaurant> updateRestaurant(@PathVariable Long id,
                                                     @Valid @RequestBody Restaurant restaurant) {
        try {
            restaurant.setId(id);
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurant);
            return ResponseEntity.ok(updatedRestaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update restaurant status", description = "Activate or deactivate restaurant")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Restaurant> updateRestaurantStatus(@PathVariable Long id,
                                                           @RequestParam Boolean isActive) {
        try {
            Restaurant restaurant = restaurantService.updateRestaurantStatus(id, isActive);
            return ResponseEntity.ok(restaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/accepting-orders")
    @Operation(summary = "Toggle order acceptance", description = "Enable or disable order acceptance")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Restaurant> toggleOrderAcceptance(@PathVariable Long id,
                                                          @RequestParam Boolean isAcceptingOrders) {
        try {
            Restaurant restaurant = restaurantService.toggleOrderAcceptance(id, isAcceptingOrders);
            return ResponseEntity.ok(restaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/delivery-settings")
    @Operation(summary = "Update delivery settings", description = "Update delivery fee and minimum order amount")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Restaurant> updateDeliverySettings(@PathVariable Long id,
                                                           @RequestParam BigDecimal deliveryFee,
                                                           @RequestParam BigDecimal minimumOrderAmount,
                                                           @RequestParam Integer estimatedDeliveryTimeMinutes) {
        try {
            Restaurant restaurant = restaurantService.updateDeliverySettings(id, deliveryFee, 
                    minimumOrderAmount, estimatedDeliveryTimeMinutes);
            return ResponseEntity.ok(restaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/rating")
    @Operation(summary = "Rate restaurant", description = "Submit rating and review for restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> rateRestaurant(@PathVariable Long id,
                                                             @RequestParam Integer rating,
                                                             @RequestParam(required = false) String review) {
        try {
            restaurantService.addRating(id, rating, review);
            return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get restaurant statistics", description = "Retrieve restaurant performance statistics")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Map<String, Object>> getRestaurantStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = restaurantService.getRestaurantStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/revenue")
    @Operation(summary = "Get restaurant revenue", description = "Calculate restaurant revenue for specified period")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#id, authentication.principal.id))")
    public ResponseEntity<Map<String, Object>> getRestaurantRevenue(@PathVariable Long id,
                                                                   @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> revenue = restaurantService.getRestaurantRevenue(id, days);
            return ResponseEntity.ok(revenue);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restaurant", description = "Permanently delete restaurant (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRestaurant(@PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}