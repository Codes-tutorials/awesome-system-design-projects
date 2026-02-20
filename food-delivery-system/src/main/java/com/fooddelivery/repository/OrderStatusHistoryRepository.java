package com.fooddelivery.repository;

import com.fooddelivery.model.OrderStatus;
import com.fooddelivery.model.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for OrderStatusHistory entity operations
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    List<OrderStatusHistory> findByOrderId(Long orderId);
    
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(Long orderId);
    
    List<OrderStatusHistory> findByStatus(OrderStatus status);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId AND osh.status = :status")
    List<OrderStatusHistory> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.createdAt >= :startDate AND osh.createdAt <= :endDate")
    List<OrderStatusHistory> findStatusChangesBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.restaurant.id = :restaurantId ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.deliveryPartner.id = :deliveryPartnerId ORDER BY osh.createdAt DESC")
    List<OrderStatusHistory> findByDeliveryPartnerId(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT COUNT(osh) FROM OrderStatusHistory osh WHERE osh.status = :status AND osh.createdAt >= :startDate AND osh.createdAt <= :endDate")
    Long countStatusChangesBetweenDates(@Param("status") OrderStatus status, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
}