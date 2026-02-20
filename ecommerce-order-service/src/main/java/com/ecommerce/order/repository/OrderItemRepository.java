package com.ecommerce.order.repository;

import com.ecommerce.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find all items for a specific order
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find items by product ID
     */
    List<OrderItem> findByProductId(String productId);
    
    /**
     * Find items by SKU
     */
    List<OrderItem> findBySku(String sku);
    
    /**
     * Find items by product ID and date range
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.productId = :productId 
        AND o.createdAt BETWEEN :startDate AND :endDate
        """)
    List<OrderItem> findByProductIdAndDateRange(@Param("productId") String productId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total quantity sold for a product
     */
    @Query(value = """
        SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.productId = :productId 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        """)
    Long getTotalQuantitySoldForProduct(@Param("productId") String productId);
    
    /**
     * Get total revenue for a product
     */
    @Query(value = """
        SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.productId = :productId 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        """)
    BigDecimal getTotalRevenueForProduct(@Param("productId") String productId);
    
    /**
     * Find top-selling products by quantity
     */
    @Query(value = """
        SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE o.createdAt >= :startDate 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        GROUP BY oi.productId, oi.productName 
        ORDER BY totalQuantity DESC
        """)
    List<Object[]> findTopSellingProductsByQuantity(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find top-selling products by revenue
     */
    @Query(value = """
        SELECT oi.productId, oi.productName, SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE o.createdAt >= :startDate 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        GROUP BY oi.productId, oi.productName 
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> findTopSellingProductsByRevenue(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find items by category
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.category = :category 
        AND o.createdAt >= :startDate
        """)
    List<OrderItem> findByCategory(@Param("category") String category,
                                  @Param("startDate") LocalDateTime startDate);
    
    /**
     * Find items by brand
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.brand = :brand 
        AND o.createdAt >= :startDate
        """)
    List<OrderItem> findByBrand(@Param("brand") String brand,
                               @Param("startDate") LocalDateTime startDate);
    
    /**
     * Find digital items
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.isDigital = true 
        AND o.status = 'CONFIRMED'
        """)
    List<OrderItem> findDigitalItems();
    
    /**
     * Find items requiring gift wrapping
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.isGiftWrap = true 
        AND o.status IN ('CONFIRMED', 'PROCESSING')
        """)
    List<OrderItem> findItemsRequiringGiftWrap();
    
    /**
     * Get category-wise sales statistics
     */
    @Query(value = """
        SELECT 
            oi.category,
            COUNT(oi) as itemCount,
            SUM(oi.quantity) as totalQuantity,
            SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE o.createdAt >= :startDate 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        GROUP BY oi.category 
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> getCategorySalesStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get brand-wise sales statistics
     */
    @Query(value = """
        SELECT 
            oi.brand,
            COUNT(oi) as itemCount,
            SUM(oi.quantity) as totalQuantity,
            SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE o.createdAt >= :startDate 
        AND o.status NOT IN ('CANCELLED', 'EXPIRED')
        GROUP BY oi.brand 
        ORDER BY totalRevenue DESC
        """)
    List<Object[]> getBrandSalesStatistics(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Find items with high unit prices (luxury items)
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        WHERE oi.unitPrice >= :minPrice 
        ORDER BY oi.unitPrice DESC
        """)
    List<OrderItem> findHighValueItems(@Param("minPrice") BigDecimal minPrice);
    
    /**
     * Find items with special instructions
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.specialInstructions IS NOT NULL 
        AND oi.specialInstructions != '' 
        AND o.status IN ('CONFIRMED', 'PROCESSING')
        """)
    List<OrderItem> findItemsWithSpecialInstructions();
    
    /**
     * Get inventory impact for a product (reserved quantities)
     */
    @Query(value = """
        SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.productId = :productId 
        AND o.status IN ('PENDING', 'CONFIRMED', 'PROCESSING')
        """)
    Long getReservedQuantityForProduct(@Param("productId") String productId);
    
    /**
     * Find items by weight range (for shipping calculations)
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        WHERE oi.weight BETWEEN :minWeight AND :maxWeight
        """)
    List<OrderItem> findByWeightRange(@Param("minWeight") BigDecimal minWeight,
                                     @Param("maxWeight") BigDecimal maxWeight);
    
    /**
     * Find items requiring special handling based on dimensions
     */
    @Query(value = """
        SELECT oi FROM OrderItem oi 
        JOIN oi.order o 
        WHERE oi.dimensions IS NOT NULL 
        AND o.status IN ('CONFIRMED', 'PROCESSING')
        """)
    List<OrderItem> findItemsWithDimensions();
    
    /**
     * Get flash sale item statistics
     */
    @Query(value = """
        SELECT 
            oi.productId,
            oi.productName,
            COUNT(oi) as orderCount,
            SUM(oi.quantity) as totalQuantity,
            SUM(oi.totalPrice) as totalRevenue
        FROM OrderItem oi 
        JOIN oi.order o 
        WHERE o.isFlashSale = true 
        AND o.flashSaleId = :flashSaleId
        GROUP BY oi.productId, oi.productName 
        ORDER BY totalQuantity DESC
        """)
    List<Object[]> getFlashSaleItemStatistics(@Param("flashSaleId") String flashSaleId);
}