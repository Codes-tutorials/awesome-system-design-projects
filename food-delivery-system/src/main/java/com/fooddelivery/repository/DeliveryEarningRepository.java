package com.fooddelivery.repository;

import com.fooddelivery.model.DeliveryEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for DeliveryEarning entity operations
 */
@Repository
public interface DeliveryEarningRepository extends JpaRepository<DeliveryEarning, Long> {
    
    List<DeliveryEarning> findByDeliveryPartnerId(Long deliveryPartnerId);
    
    List<DeliveryEarning> findByOrderId(Long orderId);
    
    @Query("SELECT de FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId ORDER BY de.createdAt DESC")
    List<DeliveryEarning> findEarningsByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT de FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId " +
           "AND de.createdAt >= :startDate AND de.createdAt <= :endDate")
    List<DeliveryEarning> findEarningsByDeliveryPartnerBetweenDates(@Param("deliveryPartnerId") Long deliveryPartnerId,
                                                                   @Param("startDate") LocalDateTime startDate,
                                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(de.totalEarning) FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId")
    BigDecimal getTotalEarningsByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT SUM(de.totalEarning) FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId " +
           "AND de.createdAt >= :startDate AND de.createdAt <= :endDate")
    BigDecimal getTotalEarningsByDeliveryPartnerBetweenDates(@Param("deliveryPartnerId") Long deliveryPartnerId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(de.totalEarning) FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId")
    BigDecimal getAverageEarningsByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT COUNT(de) FROM DeliveryEarning de WHERE de.deliveryPartner.id = :deliveryPartnerId")
    Long countEarningsByDeliveryPartner(@Param("deliveryPartnerId") Long deliveryPartnerId);
    
    @Query("SELECT de FROM DeliveryEarning de WHERE de.createdAt >= :startDate AND de.createdAt <= :endDate")
    List<DeliveryEarning> findEarningsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(de.totalEarning) FROM DeliveryEarning de WHERE de.createdAt >= :startDate AND de.createdAt <= :endDate")
    BigDecimal getTotalEarningsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT de.deliveryPartner.id, SUM(de.totalEarning) FROM DeliveryEarning de " +
           "WHERE de.createdAt >= :startDate AND de.createdAt <= :endDate " +
           "GROUP BY de.deliveryPartner.id ORDER BY SUM(de.totalEarning) DESC")
    List<Object[]> getTopEarnersBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
}