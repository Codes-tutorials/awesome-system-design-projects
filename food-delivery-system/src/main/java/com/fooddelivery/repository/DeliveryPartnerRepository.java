package com.fooddelivery.repository;

import com.fooddelivery.model.AvailabilityStatus;
import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeliveryPartner entity operations
 */
@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    
    Optional<DeliveryPartner> findByPartnerId(String partnerId);
    
    Optional<DeliveryPartner> findByUserId(Long userId);
    
    List<DeliveryPartner> findByAvailabilityStatus(AvailabilityStatus status);
    
    List<DeliveryPartner> findByVehicleType(VehicleType vehicleType);
    
    List<DeliveryPartner> findByIsOnline(Boolean isOnline);
    
    List<DeliveryPartner> findByIsActive(Boolean isActive);
    
    List<DeliveryPartner> findByIsVerified(Boolean isVerified);
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isOnline = true AND dp.isActive = true AND dp.isVerified = true " +
           "AND dp.availabilityStatus = 'AVAILABLE'")
    List<DeliveryPartner> findAvailablePartners();
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isOnline = true AND dp.isActive = true AND dp.isVerified = true " +
           "AND dp.availabilityStatus = 'AVAILABLE' AND dp.vehicleType = :vehicleType")
    List<DeliveryPartner> findAvailablePartnersByVehicleType(@Param("vehicleType") VehicleType vehicleType);
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isOnline = true AND dp.isActive = true AND dp.isVerified = true " +
           "AND dp.availabilityStatus = 'AVAILABLE' " +
           "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(dp.currentLatitude)) * " +
           "cos(radians(dp.currentLongitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(dp.currentLatitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(dp.currentLatitude)) * " +
           "cos(radians(dp.currentLongitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(dp.currentLatitude))))")
    List<DeliveryPartner> findNearbyAvailablePartners(@Param("latitude") Double latitude,
                                                      @Param("longitude") Double longitude,
                                                      @Param("radiusKm") Double radiusKm);
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isActive = true AND dp.averageRating >= :minRating " +
           "ORDER BY dp.averageRating DESC, dp.totalDeliveries DESC")
    List<DeliveryPartner> findTopRatedPartners(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.isActive = true " +
           "ORDER BY dp.totalDeliveries DESC, dp.averageRating DESC")
    List<DeliveryPartner> findMostExperiencedPartners();
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.lastActiveAt < :cutoffTime AND dp.isOnline = true")
    List<DeliveryPartner> findInactiveOnlinePartners(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.licenseExpiryDate <= :expiryDate AND dp.isActive = true")
    List<DeliveryPartner> findPartnersWithExpiringLicenses(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT COUNT(dp) FROM DeliveryPartner dp WHERE dp.isActive = true")
    Long countActivePartners();
    
    @Query("SELECT COUNT(dp) FROM DeliveryPartner dp WHERE dp.isOnline = true AND dp.isActive = true")
    Long countOnlinePartners();
    
    @Query("SELECT COUNT(dp) FROM DeliveryPartner dp WHERE dp.isOnline = true AND dp.isActive = true " +
           "AND dp.availabilityStatus = 'AVAILABLE'")
    Long countAvailablePartners();
    
    @Query("SELECT dp.vehicleType, COUNT(dp) FROM DeliveryPartner dp WHERE dp.isActive = true GROUP BY dp.vehicleType")
    List<Object[]> countPartnersByVehicleType();
    
    @Query("SELECT dp.availabilityStatus, COUNT(dp) FROM DeliveryPartner dp WHERE dp.isActive = true GROUP BY dp.availabilityStatus")
    List<Object[]> countPartnersByAvailabilityStatus();
    
    @Query("SELECT AVG(dp.averageRating) FROM DeliveryPartner dp WHERE dp.isActive = true AND dp.totalRatings > 0")
    BigDecimal getAveragePartnerRating();
    
    @Query("SELECT SUM(dp.totalEarnings) FROM DeliveryPartner dp WHERE dp.isActive = true")
    BigDecimal getTotalPartnerEarnings();
    
    @Query("SELECT dp FROM DeliveryPartner dp WHERE dp.currentMonthEarnings >= :minEarnings " +
           "ORDER BY dp.currentMonthEarnings DESC")
    List<DeliveryPartner> findTopEarningPartners(@Param("minEarnings") BigDecimal minEarnings);
}