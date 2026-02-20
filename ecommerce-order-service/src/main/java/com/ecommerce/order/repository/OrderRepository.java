package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find order by order ID with items eagerly loaded
     */
    @Query(value = """
        SELECT o FROM Order o 
        LEFT JOIN FETCH o.items 
        WHERE o.orderId = :orderId
        """)
    Optional<Order> findByOrderIdWithItems(@Param("orderId") String orderId);
    
    /**
     * Find order by order ID
     */
    Optional<Order> findByOrderId(String orderId);
    
    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by user ID with pagination
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.userId = :userId 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserId(@Param("userId") String userId, Pageable pageable);
    
    /**
     * Find orders by user ID and status
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.userId = :userId 
        AND o.status IN :statuses 
        ORDER BY o.createdAt DESC
        """)
    Page<Order> findByUserIdAndStatusIn(@Param("userId") String userId,
                                       @Param("statuses") List<OrderStatus> statuses,
                                       Pageable pageable);
    
    /**
     * Find orders by status with pagination
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by multiple statuses
     */
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    /**
     * Count orders by status
     */
    long countByStatusIn(List<OrderStatus> statuses);
    
    /**
     * Find flash sale orders
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.flashSaleId = :flashSaleId 
        ORDER BY o.createdAt ASC
        """)
    List<Order> findByFlashSaleId(@Param("flashSaleId") String flashSaleId);
    
    /**
     * Count flash sale orders by status
     */
    @Query(value = """
        SELECT COUNT(o) FROM Order o 
        WHERE o.flashSaleId = :flashSaleId 
        AND o.status = :status
        """)
    long countByFlashSaleIdAndStatus(@Param("flashSaleId") String flashSaleId, 
                                    @Param("status") OrderStatus status);
    
    /**
     * Find orders created within date range
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY o.createdAt DESC
        """)
    List<Order> findOrdersInDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find pending orders for processing (batch processing)
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.status = 'PENDING' 
        AND o.createdAt < :cutoffTime 
        ORDER BY o.priority DESC, o.createdAt ASC
        """)
    List<Order> findPendingOrdersForProcessing(@Param("cutoffTime") LocalDateTime cutoffTime, 
                                              Pageable pageable);
    
    /**
     * Find orders by shard key for sharding
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.shardKey = :shardKey 
        AND o.status = :status
        """)
    List<Order> findByShardKeyAndStatus(@Param("shardKey") Integer shardKey, 
                                       @Param("status") OrderStatus status);
    
    /**
     * Update order status with optimistic locking
     */
    @Modifying
    @Query(value = """
        UPDATE orders 
        SET status = :status, updated_at = CURRENT_TIMESTAMP, version = version + 1
        WHERE order_id = :orderId AND version = :version
        """, nativeQuery = true)
    int updateOrderStatus(@Param("orderId") String orderId,
                         @Param("status") String status,
                         @Param("version") Long version);
    
    /**
     * Update order payment information
     */
    @Modifying
    @Query(value = """
        UPDATE orders 
        SET payment_id = :paymentId, payment_method = :paymentMethod, 
            updated_at = CURRENT_TIMESTAMP, version = version + 1
        WHERE order_id = :orderId
        """, nativeQuery = true)
    int updateOrderPaymentInfo(@Param("orderId") String orderId,
                              @Param("paymentId") String paymentId,
                              @Param("paymentMethod") String paymentMethod);
    
    /**
     * Bulk update order statuses
     */
    @Modifying
    @Query(value = """
        UPDATE orders 
        SET status = :newStatus, updated_at = CURRENT_TIMESTAMP, version = version + 1
        WHERE status = :currentStatus 
        AND created_at < :cutoffTime
        """, nativeQuery = true)
    int bulkUpdateOrderStatus(@Param("currentStatus") String currentStatus,
                             @Param("newStatus") String newStatus,
                             @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find orders requiring cleanup (cancelled/expired orders)
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE (o.status = 'CANCELLED' OR o.status = 'EXPIRED') 
        AND o.updatedAt < :cutoffTime
        """)
    List<Order> findOrdersForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Get order statistics for dashboard
     */
    @Query(value = """
        SELECT 
            o.status as status,
            COUNT(o) as count,
            SUM(o.totalAmount) as totalAmount
        FROM Order o 
        WHERE o.createdAt >= :startDate 
        GROUP BY o.status
        """)
    List<Object[]> getOrderStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find high-value orders for special handling
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.totalAmount >= :minAmount 
        AND o.status IN :statuses 
        ORDER BY o.totalAmount DESC
        """)
    List<Order> findHighValueOrders(@Param("minAmount") java.math.BigDecimal minAmount,
                                   @Param("statuses") List<OrderStatus> statuses);
    
    /**
     * Find orders by user and date range for analytics
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.userId = :userId 
        AND o.createdAt BETWEEN :startDate AND :endDate 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        ORDER BY o.createdAt DESC
        """)
    List<Order> findUserOrdersInDateRange(@Param("userId") String userId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Check if order exists by order number (for duplicate prevention)
     */
    boolean existsByOrderNumber(String orderNumber);
    
    /**
     * Check if user has pending orders
     */
    @Query(value = """
        SELECT COUNT(o) > 0 FROM Order o 
        WHERE o.userId = :userId 
        AND o.status IN ('PENDING', 'PROCESSING')
        """)
    boolean hasUserPendingOrders(@Param("userId") String userId);
    
    /**
     * Find orders with payment issues for retry
     */
    @Query(value = """
        SELECT o FROM Order o 
        WHERE o.status = 'PAYMENT_FAILED' 
        AND o.updatedAt < :retryAfter 
        ORDER BY o.createdAt ASC
        """)
    List<Order> findOrdersForPaymentRetry(@Param("retryAfter") LocalDateTime retryAfter);
}