package com.rideshare.repository;

import com.rideshare.model.Driver;
import com.rideshare.model.Trip;
import com.rideshare.model.TripStatus;
import com.rideshare.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Trip entity
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    /**
     * Find trip by public trip ID
     */
    Optional<Trip> findByTripId(String tripId);
    
    /**
     * Find current active trip for rider
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.rider = :rider 
        AND t.status IN ('REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')
        ORDER BY t.requestedAt DESC
        """)
    Optional<Trip> findCurrentTripByRider(@Param("rider") User rider);
    
    /**
     * Find current active trip for driver
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.driver = :driver 
        AND t.status IN ('ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')
        ORDER BY t.acceptedAt DESC
        """)
    Optional<Trip> findCurrentTripByDriver(@Param("driver") Driver driver);
    
    /**
     * Check if driver has active trip
     */
    boolean existsByDriverAndStatusIn(Driver driver, List<TripStatus> statuses);
    
    /**
     * Check if rider has active trip
     */
    boolean existsByRiderAndStatusIn(User rider, List<TripStatus> statuses);
    
    /**
     * Find trips by rider with pagination
     */
    Page<Trip> findByRiderOrderByRequestedAtDesc(User rider, Pageable pageable);
    
    /**
     * Find trips by driver with pagination
     */
    Page<Trip> findByDriverOrderByAcceptedAtDesc(Driver driver, Pageable pageable);
    
    /**
     * Find trips by status
     */
    List<Trip> findByStatus(TripStatus status);
    
    /**
     * Find trips by status and date range
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status = :status 
        AND t.requestedAt BETWEEN :startDate AND :endDate
        ORDER BY t.requestedAt DESC
        """)
    List<Trip> findByStatusAndDateRange(
        @Param("status") TripStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find completed trips for rider
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.rider = :rider 
        AND t.status = 'COMPLETED'
        ORDER BY t.tripCompletedAt DESC
        """)
    Page<Trip> findCompletedTripsByRider(@Param("rider") User rider, Pageable pageable);
    
    /**
     * Find completed trips for driver
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.driver = :driver 
        AND t.status = 'COMPLETED'
        ORDER BY t.tripCompletedAt DESC
        """)
    Page<Trip> findCompletedTripsByDriver(@Param("driver") Driver driver, Pageable pageable);
    
    /**
     * Find trips that need timeout handling
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status = 'REQUESTED' 
        AND t.requestedAt < :cutoffTime
        """)
    List<Trip> findTripsNeedingTimeout(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find trips in area for surge calculation
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.status IN ('REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVED', 'IN_PROGRESS')
        AND t.requestedAt > :since
        AND (
            6371 * acos(
                cos(radians(:latitude)) * cos(radians(t.pickupLatitude)) *
                cos(radians(t.pickupLongitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(t.pickupLatitude))
            )
        ) <= :radiusKm
        """)
    List<Trip> findActiveTripsInArea(
        @Param("latitude") Double latitude,
        @Param("longitude") Double longitude,
        @Param("radiusKm") Double radiusKm,
        @Param("since") LocalDateTime since
    );
    
    /**
     * Count trips by status in date range
     */
    @Query("""
        SELECT COUNT(t) FROM Trip t 
        WHERE t.status = :status 
        AND t.requestedAt BETWEEN :startDate AND :endDate
        """)
    long countTripsByStatusInDateRange(
        @Param("status") TripStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find trips with high surge multiplier
     */
    @Query("SELECT t FROM Trip t WHERE t.surgeMultiplier > 1.5 ORDER BY t.surgeMultiplier DESC")
    List<Trip> findTripsWithHighSurge();
    
    /**
     * Find unrated trips for rider
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.rider = :rider 
        AND t.status = 'COMPLETED'
        AND t.riderRating IS NULL
        ORDER BY t.tripCompletedAt DESC
        """)
    List<Trip> findUnratedTripsByRider(@Param("rider") User rider);
    
    /**
     * Find unrated trips for driver
     */
    @Query("""
        SELECT t FROM Trip t 
        WHERE t.driver = :driver 
        AND t.status = 'COMPLETED'
        AND t.driverRating IS NULL
        ORDER BY t.tripCompletedAt DESC
        """)
    List<Trip> findUnratedTripsByDriver(@Param("driver") Driver driver);
    
    /**
     * Calculate average trip duration
     */
    @Query("SELECT AVG(t.actualDurationMinutes) FROM Trip t WHERE t.status = 'COMPLETED' AND t.actualDurationMinutes IS NOT NULL")
    Double calculateAverageTripDuration();
    
    /**
     * Calculate average trip distance
     */
    @Query("SELECT AVG(t.actualDistanceKm) FROM Trip t WHERE t.status = 'COMPLETED' AND t.actualDistanceKm IS NOT NULL")
    Double calculateAverageTripDistance();
    
    /**
     * Find trips by payment status
     */
    @Query("SELECT t FROM Trip t WHERE t.paymentStatus = :paymentStatus")
    List<Trip> findTripsByPaymentStatus(@Param("paymentStatus") com.rideshare.model.PaymentStatus paymentStatus);
}