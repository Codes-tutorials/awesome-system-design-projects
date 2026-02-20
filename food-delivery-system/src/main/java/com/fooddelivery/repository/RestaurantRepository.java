package com.fooddelivery.repository;

import com.fooddelivery.model.CuisineType;
import com.fooddelivery.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Restaurant entity operations
 */
@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    Optional<Restaurant> findByUserId(Long userId);
    
    List<Restaurant> findByIsActive(Boolean isActive);
    
    List<Restaurant> findByIsAcceptingOrders(Boolean isAcceptingOrders);
    
    List<Restaurant> findByIsFeatured(Boolean isFeatured);
    
    List<Restaurant> findByCuisineType(CuisineType cuisineType);
    
    List<Restaurant> findByCity(String city);
    
    List<Restaurant> findByCityAndIsActive(String city, Boolean isActive);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true")
    List<Restaurant> findAvailableRestaurants();
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true AND r.city = :city")
    Page<Restaurant> findAvailableRestaurantsByCity(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT r FROM Restaurant r WHERE r.averageRating >= :minRating AND r.isActive = true ORDER BY r.averageRating DESC")
    List<Restaurant> findByMinimumRating(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true " +
           "AND (:cuisineType IS NULL OR r.cuisineType = :cuisineType) " +
           "AND (:city IS NULL OR r.city = :city) " +
           "AND (:minRating IS NULL OR r.averageRating >= :minRating) " +
           "ORDER BY r.averageRating DESC, r.totalRatings DESC")
    Page<Restaurant> findRestaurantsWithFilters(@Param("cuisineType") CuisineType cuisineType,
                                               @Param("city") String city,
                                               @Param("minRating") BigDecimal minRating,
                                               Pageable pageable);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true " +
           "AND LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Restaurant> searchByName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true " +
           "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(r.latitude)) * " +
           "cos(radians(r.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(r.latitude)))) <= :radiusKm")
    List<Restaurant> findNearbyRestaurants(@Param("latitude") Double latitude,
                                          @Param("longitude") Double longitude,
                                          @Param("radiusKm") Double radiusKm);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true " +
           "AND r.deliveryFee <= :maxDeliveryFee ORDER BY r.deliveryFee ASC")
    List<Restaurant> findByMaxDeliveryFee(@Param("maxDeliveryFee") BigDecimal maxDeliveryFee);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true " +
           "AND r.minimumOrderAmount <= :maxMinimumOrder ORDER BY r.minimumOrderAmount ASC")
    List<Restaurant> findByMaxMinimumOrderAmount(@Param("maxMinimumOrder") BigDecimal maxMinimumOrder);
    
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.isActive = true")
    Long countActiveRestaurants();
    
    @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.isActive = true AND r.isAcceptingOrders = true")
    Long countAvailableRestaurants();
    
    @Query("SELECT r.cuisineType, COUNT(r) FROM Restaurant r WHERE r.isActive = true GROUP BY r.cuisineType")
    List<Object[]> countRestaurantsByCuisineType();
}