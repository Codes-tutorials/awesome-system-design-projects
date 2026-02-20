package com.fooddelivery.repository;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderStatus;
import com.fooddelivery.model.PaymentStatus;
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
 * Repository interface for Order entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByRestaurantId(Long restaurantId);
    
    List<Order> findByDeliveryPartnerId(Long deliveryPartnerId);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByCustomer(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByRestaurant(@Param("restaurantId") Long restaurantId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findOrdersByCustomerAndStatus(@Param("customerId") Long customerId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = :status")
    List<Order> findOrdersByRestaurantAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId AND o.status = :status")
    List<Order> findOrdersByDeliveryPartnerAndStatus(@Param("deliveryPartnerId") Long deliveryPartnerId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders(@Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByRestaurant(@Param("restaurantId") Long restaurantId, @Param("statuses") List<OrderStatus> statuses);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findOrdersByRestaurantBetweenDates(@Param("restaurantId") Long restaurantId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findOrdersByDeliveryPartnerBetweenDates(@Param("deliveryPartnerId") Long deliveryPartnerId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryTime < :currentTime AND o.status NOT IN ('DELIVERED', 'CANCELLED')")
    List<Order> findDelayedOrders(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.customer.id = :customerId")
    Long countOrdersByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId")
    Long countOrdersByRestaurant(@Param("restaurantId") Long restaurantId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId")
    Long countOrdersByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countOrdersByStatus(@Param("status") OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.status = 'DELIVERED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal calculateRestaurantRevenue(@Param("restaurantId") Long restaurantId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(o.deliveryFee) FROM Order o WHERE o.deliveryPartner.id = :deliveryPartnerId AND o.status = 'DELIVERED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal calculateDeliveryPartnerEarnings(@Param("deliveryPartnerId") Long deliveryPartnerId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getAverageOrderValue();
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusStatistics();
    
    @Query("SELECT DATE(o.createdAt), COUNT(o) FROM Order o WHERE o.createdAt >= :startDate GROUP BY DATE(o.createdAt) ORDER BY DATE(o.createdAt)")
    List<Object[]> getDailyOrderCounts(@Param("startDate") LocalDateTime startDate);
}