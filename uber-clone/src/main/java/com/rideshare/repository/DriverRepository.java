package com.rideshare.repository;

import com.rideshare.model.AvailabilityStatus;
import com.rideshare.model.Driver;
import com.rideshare.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Driver entity
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    /**
     * Find driver by user ID
     */
    Optional<Driver> findByUserId(Long userId);
    
    /**
     * Find available drivers within radius using spatial query
     * This is a simplified version - in production, you'd use PostGIS or similar
     */
    @Query("""
        SELECT d FROM Driver d 
        JOIN d.user u 
        WHERE d.availabilityStatus = 'AVAILABLE' 
        AND d.isOnline = true 
        AND d.isVerified = true 
        AND u.isActive = true
        AND d.locationUpdatedAt > :minLocationTime
        AND (:vehicleType IS NULL OR EXISTS (
            SELECT v FROM Vehicle v 
            WHERE v.driver = d 
            AND v.vehicleType = :vehicleType 
            AND v.isActive = true
        ))
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.currentLatitude)) *
                cos(radians(d.currentLongitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.currentLatitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.currentLatitude)) *
                cos(radians(d.currentLongitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.currentLatitude))
            )
        )
        """)
    List<Driver> findAvailableDriversWithinRadius(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm,
        @Param("vehicleType") VehicleType vehicleType,
        @Param("minLocationTime") LocalDateTime minLocationTime
    );
    
    /**
     * Simplified version without vehicle type filter
     */
    default List<Driver> findAvailableDriversWithinRadius(
            Double latitude, Double longitude, Double radiusKm, VehicleType vehicleType) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return findAvailableDriversWithinRadius(latitude, longitude, radiusKm, vehicleType, fiveMinutesAgo);
    }
    
    /**
     * Find drivers by availability status
     */
    List<Driver> findByAvailabilityStatus(AvailabilityStatus status);
    
    /**
     * Find online drivers
     */
    @Query("SELECT d FROM Driver d WHERE d.isOnline = true AND d.user.isActive = true")
    List<Driver> findOnlineDrivers();
    
    /**
     * Find drivers with high ratings
     */
    @Query("SELECT d FROM Driver d WHERE d.averageRating >= :minRating ORDER BY d.averageRating DESC")
    List<Driver> findDriversWithHighRating(@Param("minRating") BigDecimal minRating);
    
    /**
     * Find drivers by license plate
     */
    @Query("SELECT d FROM Driver d JOIN Vehicle v ON v.driver = d WHERE v.licensePlate = :licensePlate")
    Optional<Driver> findByVehicleLicensePlate(@Param("licensePlate") String licensePlate);
    
    /**
     * Count available drivers in area
     */
    @Query("""
        SELECT COUNT(d) FROM Driver d 
        WHERE d.availabilityStatus = 'AVAILABLE' 
        AND d.isOnline = true 
        AND d.isVerified = true
        AND d.locationUpdatedAt > :minLocationTime
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(d.currentLatitude)) *
                cos(radians(d.currentLongitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(d.currentLatitude))
            )
        ) <= :radiusKm
        """)
    long countAvailableDriversInArea(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm,
        @Param("minLocationTime") LocalDateTime minLocationTime
    );
    
    /**
     * Find drivers who haven't updated location recently
     */
    @Query("SELECT d FROM Driver d WHERE d.locationUpdatedAt < :cutoffTime AND d.isOnline = true")
    List<Driver> findDriversWithStaleLocation(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Update driver availability status
     */
    @Query("UPDATE Driver d SET d.availabilityStatus = :status WHERE d.id = :driverId")
    void updateAvailabilityStatus(@Param("driverId") Long driverId, @Param("status") AvailabilityStatus status);
    
    /**
     * Find top rated drivers
     */
    @Query("SELECT d FROM Driver d WHERE d.averageRating IS NOT NULL ORDER BY d.averageRating DESC")
    List<Driver> findTopRatedDrivers();
    
    /**
     * Find drivers by verification status
     */
    List<Driver> findByIsVerified(boolean isVerified);
}