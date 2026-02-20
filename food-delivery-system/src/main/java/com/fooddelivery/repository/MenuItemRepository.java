package com.fooddelivery.repository;

import com.fooddelivery.model.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for MenuItem entity operations
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    List<MenuItem> findByRestaurantId(Long restaurantId);
    
    List<MenuItem> findByRestaurantIdAndIsAvailable(Long restaurantId, Boolean isAvailable);
    
    List<MenuItem> findByCategory(String category);
    
    List<MenuItem> findByIsVegetarian(Boolean isVegetarian);
    
    List<MenuItem> findByIsVegan(Boolean isVegan);
    
    List<MenuItem> findByIsGlutenFree(Boolean isGlutenFree);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true ORDER BY m.category, m.name")
    List<MenuItem> findAvailableMenuItemsByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.category = :category AND m.isAvailable = true")
    List<MenuItem> findAvailableMenuItemsByRestaurantAndCategory(@Param("restaurantId") Long restaurantId, 
                                                                @Param("category") String category);
    
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MenuItem> searchByName(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<MenuItem> searchMenuItems(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.price BETWEEN :minPrice AND :maxPrice")
    List<MenuItem> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true " +
           "AND (:category IS NULL OR m.category = :category) " +
           "AND (:isVegetarian IS NULL OR m.isVegetarian = :isVegetarian) " +
           "AND (:isVegan IS NULL OR m.isVegan = :isVegan) " +
           "AND (:isGlutenFree IS NULL OR m.isGlutenFree = :isGlutenFree) " +
           "AND (:minPrice IS NULL OR m.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR m.price <= :maxPrice)")
    List<MenuItem> findMenuItemsWithFilters(@Param("restaurantId") Long restaurantId,
                                           @Param("category") String category,
                                           @Param("isVegetarian") Boolean isVegetarian,
                                           @Param("isVegan") Boolean isVegan,
                                           @Param("isGlutenFree") Boolean isGlutenFree,
                                           @Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice);
    
    @Query("SELECT DISTINCT m.category FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true ORDER BY m.category")
    List<String> findCategoriesByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT m FROM MenuItem m WHERE m.isAvailable = true AND m.isFeatured = true ORDER BY m.createdAt DESC")
    List<MenuItem> findFeaturedMenuItems();
    
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.city = :city AND m.isAvailable = true " +
           "AND LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY m.restaurant.averageRating DESC")
    List<MenuItem> searchMenuItemsByCity(@Param("city") String city, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(m) FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.isAvailable = true")
    Long countAvailableMenuItemsByRestaurant(@Param("restaurantId") Long restaurantId);
}