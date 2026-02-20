package com.fooddelivery.repository;

import com.fooddelivery.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for OrderItem entity operations
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByMenuItemId(Long menuItemId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.restaurant.id = :restaurantId")
    List<OrderItem> findByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.customer.id = :customerId")
    List<OrderItem> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT oi.menuItem.id, SUM(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' AND oi.order.createdAt >= :startDate AND oi.order.createdAt <= :endDate " +
           "GROUP BY oi.menuItem.id ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostOrderedItems(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT oi.menuItem.id, SUM(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.order.restaurant.id = :restaurantId AND oi.order.status = 'DELIVERED' " +
           "AND oi.order.createdAt >= :startDate AND oi.order.createdAt <= :endDate " +
           "GROUP BY oi.menuItem.id ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostOrderedItemsByRestaurant(@Param("restaurantId") Long restaurantId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(oi.quantity * oi.unitPrice) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Double calculateOrderSubtotal(@Param("orderId") Long orderId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.menuItem.id = :menuItemId")
    Long countOrdersByMenuItem(@Param("menuItemId") Long menuItemId);
}