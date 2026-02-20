package com.fooddelivery.repository;

import com.fooddelivery.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Review entity operations
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByRestaurantId(Long restaurantId);
    
    List<Review> findByCustomerId(Long customerId);
    
    List<Review> findByOrderId(Long orderId);
    
    Optional<Review> findByOrderIdAndCustomerId(Long orderId, Long customerId);
    
    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId ORDER BY r.createdAt DESC")
    Page<Review> findReviewsByRestaurant(@Param("restaurantId") Long restaurantId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.customer.id = :customerId ORDER BY r.createdAt DESC")
    Page<Review> findReviewsByCustomer(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId AND r.rating >= :minRating")
    List<Review> findReviewsByRestaurantAndMinRating(@Param("restaurantId") Long restaurantId, @Param("minRating") Integer minRating);
    
    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId AND r.rating = :rating")
    List<Review> findReviewsByRestaurantAndRating(@Param("restaurantId") Long restaurantId, @Param("rating") Integer rating);
    
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :startDate AND r.createdAt <= :endDate")
    List<Review> findReviewsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    BigDecimal getAverageRatingByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Long countReviewsByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT r FROM Review r WHERE r.restaurant.id = :restaurantId AND r.comment IS NOT NULL AND r.comment != '' ORDER BY r.createdAt DESC")
    List<Review> findReviewsWithCommentsByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT r FROM Review r WHERE r.rating <= :maxRating ORDER BY r.createdAt DESC")
    List<Review> findLowRatedReviews(@Param("maxRating") Integer maxRating);
    
    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating ORDER BY r.createdAt DESC")
    List<Review> findHighRatedReviews(@Param("minRating") Integer minRating);
    
    boolean existsByOrderIdAndCustomerId(Long orderId, Long customerId);
}