package com.fooddelivery.repository;

import com.fooddelivery.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUserId(Long userId);
    
    Optional<CartItem> findByUserIdAndMenuItemId(Long userId, Long menuItemId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.user.id = :userId AND ci.menuItem.restaurant.id = :restaurantId")
    List<CartItem> findByUserIdAndRestaurantId(@Param("userId") Long userId, @Param("restaurantId") Long restaurantId);
    
    @Query("SELECT SUM(ci.quantity * ci.menuItem.price) FROM CartItem ci WHERE ci.user.id = :userId")
    Double calculateCartTotal(@Param("userId") Long userId);
    
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.user.id = :userId")
    Integer calculateCartItemCount(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(DISTINCT ci.menuItem.restaurant.id) FROM CartItem ci WHERE ci.user.id = :userId")
    Long countDistinctRestaurantsInCart(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
    void clearCartByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId AND ci.menuItem.restaurant.id = :restaurantId")
    void clearCartByUserIdAndRestaurantId(@Param("userId") Long userId, @Param("restaurantId") Long restaurantId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.menuItem.id = :menuItemId")
    List<CartItem> findByMenuItemId(@Param("menuItemId") Long menuItemId);
}