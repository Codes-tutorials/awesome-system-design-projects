package com.fooddelivery.service;

import com.fooddelivery.model.CuisineType;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Restaurant entity operations
 */
@Service
@Transactional
public class RestaurantService {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocationService locationService;
    
    public Restaurant createRestaurant(Long ownerId, String name, String description, String phoneNumber,
                                     String email, CuisineType cuisineType, String address, String city,
                                     String state, String postalCode, String country) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + ownerId));
        
        if (!owner.isRestaurantOwner()) {
            throw new RuntimeException("User is not a restaurant owner");
        }
        
        // Check if user already has a restaurant
        if (restaurantRepository.findByUserId(ownerId).isPresent()) {
            throw new RuntimeException("User already has a restaurant");
        }
        
        Restaurant restaurant = new Restaurant(name, phoneNumber, cuisineType, address, city, state, postalCode, country);
        restaurant.setUser(owner);
        restaurant.setDescription(description);
        restaurant.setEmail(email);
        
        // Get coordinates for the address
        try {
            double[] coordinates = locationService.getCoordinates(restaurant.getFullAddress());
            restaurant.setLatitude(coordinates[0]);
            restaurant.setLongitude(coordinates[1]);
        } catch (Exception e) {
            // Log error but don't fail restaurant creation
            System.err.println("Failed to get coordinates for restaurant: " + e.getMessage());
        }
        
        return restaurantRepository.save(restaurant);
    }
    
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }
    
    public Optional<Restaurant> findByOwnerId(Long ownerId) {
        return restaurantRepository.findByUserId(ownerId);
    }
    
    public Restaurant updateRestaurant(Long restaurantId, String name, String description, String phoneNumber,
                                     String email, String address, String city, String state, String postalCode) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setPhoneNumber(phoneNumber);
        restaurant.setEmail(email);
        restaurant.setAddress(address);
        restaurant.setCity(city);
        restaurant.setState(state);
        restaurant.setPostalCode(postalCode);
        
        // Update coordinates if address changed
        try {
            double[] coordinates = locationService.getCoordinates(restaurant.getFullAddress());
            restaurant.setLatitude(coordinates[0]);
            restaurant.setLongitude(coordinates[1]);
        } catch (Exception e) {
            System.err.println("Failed to update coordinates for restaurant: " + e.getMessage());
        }
        
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant updateRestaurantImages(Long restaurantId, String imageUrl, String coverImageUrl) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        if (imageUrl != null) {
            restaurant.setImageUrl(imageUrl);
        }
        if (coverImageUrl != null) {
            restaurant.setCoverImageUrl(coverImageUrl);
        }
        
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant updateOperatingHours(Long restaurantId, LocalTime openingTime, LocalTime closingTime) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setOpeningTime(openingTime);
        restaurant.setClosingTime(closingTime);
        
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant updateDeliverySettings(Long restaurantId, BigDecimal deliveryFee, 
                                           BigDecimal minimumOrderAmount, Integer estimatedDeliveryTime) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setDeliveryFee(deliveryFee);
        restaurant.setMinimumOrderAmount(minimumOrderAmount);
        restaurant.setEstimatedDeliveryTimeMinutes(estimatedDeliveryTime);
        
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant toggleAcceptingOrders(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setIsAcceptingOrders(!restaurant.getIsAcceptingOrders());
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant setFeaturedStatus(Long restaurantId, boolean isFeatured) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setIsFeatured(isFeatured);
        return restaurantRepository.save(restaurant);
    }
    
    public List<Restaurant> findAvailableRestaurants() {
        return restaurantRepository.findAvailableRestaurants();
    }
    
    public Page<Restaurant> findAvailableRestaurantsByCity(String city, Pageable pageable) {
        return restaurantRepository.findAvailableRestaurantsByCity(city, pageable);
    }
    
    public List<Restaurant> findRestaurantsByCuisine(CuisineType cuisineType) {
        return restaurantRepository.findByCuisineType(cuisineType);
    }
    
    public List<Restaurant> searchRestaurantsByName(String searchTerm) {
        return restaurantRepository.searchByName(searchTerm);
    }
    
    public List<Restaurant> findNearbyRestaurants(Double latitude, Double longitude, Double radiusKm) {
        return restaurantRepository.findNearbyRestaurants(latitude, longitude, radiusKm);
    }
    
    public Page<Restaurant> findRestaurantsWithFilters(CuisineType cuisineType, String city, 
                                                      BigDecimal minRating, Pageable pageable) {
        return restaurantRepository.findRestaurantsWithFilters(cuisineType, city, minRating, pageable);
    }
    
    public List<Restaurant> findByMaxDeliveryFee(BigDecimal maxDeliveryFee) {
        return restaurantRepository.findByMaxDeliveryFee(maxDeliveryFee);
    }
    
    public List<Restaurant> findByMaxMinimumOrderAmount(BigDecimal maxMinimumOrder) {
        return restaurantRepository.findByMaxMinimumOrderAmount(maxMinimumOrder);
    }
    
    public List<Restaurant> findTopRatedRestaurants(BigDecimal minRating) {
        return restaurantRepository.findByMinimumRating(minRating);
    }
    
    public List<Restaurant> findFeaturedRestaurants() {
        return restaurantRepository.findByIsFeatured(true);
    }
    
    public void updateRestaurantRating(Long restaurantId, BigDecimal newRating) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.updateRating(newRating);
        restaurantRepository.save(restaurant);
    }
    
    public Restaurant activateRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setIsActive(true);
        return restaurantRepository.save(restaurant);
    }
    
    public Restaurant deactivateRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setIsActive(false);
        restaurant.setIsAcceptingOrders(false);
        return restaurantRepository.save(restaurant);
    }
    
    public Long countActiveRestaurants() {
        return restaurantRepository.countActiveRestaurants();
    }
    
    public Long countAvailableRestaurants() {
        return restaurantRepository.countAvailableRestaurants();
    }
    
    public List<Object[]> getRestaurantStatsByCuisine() {
        return restaurantRepository.countRestaurantsByCuisineType();
    }
}