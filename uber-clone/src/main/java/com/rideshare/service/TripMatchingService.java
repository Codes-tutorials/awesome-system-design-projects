package com.rideshare.service;

import com.rideshare.model.*;
import com.rideshare.repository.DriverRepository;
import com.rideshare.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service for matching riders with available drivers
 * Implements sophisticated algorithms for optimal driver selection
 */
@Service
@Transactional
public class TripMatchingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TripMatchingService.class);
    private static final double MAX_SEARCH_RADIUS_KM = 10.0; // Maximum search radius
    private static final int MAX_DRIVERS_TO_NOTIFY = 5; // Maximum drivers to notify per trip
    private static final int DRIVER_RESPONSE_TIMEOUT_SECONDS = 30; // Time for driver to respond
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Find and notify available drivers for a trip request
     */
    public void findAndNotifyDrivers(Trip trip) {
        logger.info("Finding drivers for trip: {}", trip.getTripId());
        
        try {
            // Calculate estimated fare first
            BigDecimal estimatedFare = pricingService.calculateEstimatedFare(trip);
            trip.setTotalFare(estimatedFare);
            
            // Find available drivers near pickup location
            List<Driver> availableDrivers = findAvailableDrivers(
                trip.getPickupLatitude(),
                trip.getPickupLongitude(),
                trip.getRequestedVehicleType(),
                MAX_SEARCH_RADIUS_KM
            );
            
            if (availableDrivers.isEmpty()) {
                logger.warn("No available drivers found for trip: {}", trip.getTripId());
                handleNoDriversAvailable(trip);
                return;
            }
            
            // Sort drivers by optimal criteria
            List<Driver> sortedDrivers = sortDriversByOptimalCriteria(
                availableDrivers, 
                trip.getPickupLatitude(), 
                trip.getPickupLongitude()
            );
            
            // Notify drivers in order of preference
            notifyDriversSequentially(trip, sortedDrivers);
            
            // Set trip timeout
            setTripTimeout(trip);
            
        } catch (Exception e) {
            logger.error("Error finding drivers for trip: {}", trip.getTripId(), e);
            handleTripMatchingError(trip, e);
        }
    }
    
    /**
     * Find available drivers within radius
     */
    private List<Driver> findAvailableDrivers(Double latitude, Double longitude, 
                                            VehicleType vehicleType, double radiusKm) {
        
        // Use spatial query to find drivers within radius
        List<Driver> nearbyDrivers = driverRepository.findAvailableDriversWithinRadius(
            latitude, longitude, radiusKm, vehicleType
        );
        
        // Filter drivers based on additional criteria
        return nearbyDrivers.stream()
            .filter(this::isDriverEligibleForTrip)
            .limit(MAX_DRIVERS_TO_NOTIFY)
            .toList();
    }
    
    /**
     * Check if driver is eligible for trip assignment
     */
    private boolean isDriverEligibleForTrip(Driver driver) {
        // Check if driver is truly available
        if (!driver.isAvailable()) {
            return false;
        }
        
        // Check if driver has active trip
        boolean hasActiveTrip = tripRepository.existsByDriverAndStatusIn(
            driver, 
            List.of(TripStatus.ACCEPTED, TripStatus.DRIVER_ARRIVED, TripStatus.IN_PROGRESS)
        );
        
        if (hasActiveTrip) {
            return false;
        }
        
        // Check driver's recent activity
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        if (driver.getLocationUpdatedAt() == null || 
            driver.getLocationUpdatedAt().isBefore(fiveMinutesAgo)) {
            return false;
        }
        
        // Check acceptance rate (should be above 70%)
        if (driver.getAcceptanceRate() != null && 
            driver.getAcceptanceRate().compareTo(new BigDecimal("70")) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Sort drivers by optimal criteria for trip assignment
     */
    private List<Driver> sortDriversByOptimalCriteria(List<Driver> drivers, 
                                                     Double pickupLat, Double pickupLng) {
        return drivers.stream()
            .sorted((d1, d2) -> {
                // Calculate composite score for each driver
                double score1 = calculateDriverScore(d1, pickupLat, pickupLng);
                double score2 = calculateDriverScore(d2, pickupLat, pickupLng);
                return Double.compare(score2, score1); // Higher score first
            })
            .toList();
    }
    
    /**
     * Calculate driver score based on multiple factors
     */
    private double calculateDriverScore(Driver driver, Double pickupLat, Double pickupLng) {
        double score = 0.0;
        
        // Distance factor (closer is better) - 40% weight
        double distance = locationService.calculateDistance(
            driver.getCurrentLatitude(), driver.getCurrentLongitude(),
            pickupLat, pickupLng
        );
        double distanceScore = Math.max(0, 10 - distance); // Max 10 points
        score += distanceScore * 0.4;
        
        // Rating factor - 30% weight
        if (driver.getAverageRating() != null) {
            double ratingScore = driver.getAverageRating().doubleValue() * 2; // Max 10 points
            score += ratingScore * 0.3;
        }
        
        // Acceptance rate factor - 20% weight
        if (driver.getAcceptanceRate() != null) {
            double acceptanceScore = driver.getAcceptanceRate().doubleValue() / 10; // Max 10 points
            score += acceptanceScore * 0.2;
        }
        
        // Recent activity factor - 10% weight
        if (driver.getLocationUpdatedAt() != null) {
            long minutesSinceUpdate = java.time.Duration.between(
                driver.getLocationUpdatedAt(), LocalDateTime.now()
            ).toMinutes();
            double activityScore = Math.max(0, 10 - minutesSinceUpdate); // Max 10 points
            score += activityScore * 0.1;
        }
        
        return score;
    }
    
    /**
     * Notify drivers sequentially with timeout
     */
    private void notifyDriversSequentially(Trip trip, List<Driver> drivers) {
        for (int i = 0; i < drivers.size() && i < MAX_DRIVERS_TO_NOTIFY; i++) {
            Driver driver = drivers.get(i);
            
            // Store driver notification in Redis for tracking
            String notificationKey = "trip_notification:" + trip.getTripId() + ":" + driver.getId();
            redisTemplate.opsForValue().set(
                notificationKey, 
                LocalDateTime.now().toString(), 
                DRIVER_RESPONSE_TIMEOUT_SECONDS, 
                TimeUnit.SECONDS
            );
            
            // Send notification to driver
            notificationService.sendTripRequestToDriver(trip, driver);
            
            logger.info("Notified driver {} for trip {}", driver.getId(), trip.getTripId());
            
            // If this is not the first driver, add delay
            if (i > 0) {
                try {
                    Thread.sleep(5000); // 5 second delay between notifications
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    /**
     * Handle driver accepting a trip
     */
    public boolean acceptTrip(String tripId, Long driverId) {
        logger.info("Driver {} attempting to accept trip {}", driverId, tripId);
        
        try {
            Optional<Trip> tripOpt = tripRepository.findByTripId(tripId);
            if (tripOpt.isEmpty()) {
                logger.warn("Trip not found: {}", tripId);
                return false;
            }
            
            Trip trip = tripOpt.get();
            
            // Check if trip is still available
            if (trip.getStatus() != TripStatus.REQUESTED) {
                logger.warn("Trip {} is no longer available, status: {}", tripId, trip.getStatus());
                return false;
            }
            
            Optional<Driver> driverOpt = driverRepository.findById(driverId);
            if (driverOpt.isEmpty()) {
                logger.warn("Driver not found: {}", driverId);
                return false;
            }
            
            Driver driver = driverOpt.get();
            
            // Verify driver is still available
            if (!driver.isAvailable()) {
                logger.warn("Driver {} is no longer available", driverId);
                return false;
            }
            
            // Accept the trip
            trip.acceptTrip(driver);
            driver.setAvailabilityStatus(AvailabilityStatus.BUSY);
            
            // Save changes
            tripRepository.save(trip);
            driverRepository.save(driver);
            
            // Notify rider about driver assignment
            notificationService.sendDriverAssignedToRider(trip);
            
            // Cancel notifications to other drivers
            cancelOtherDriverNotifications(trip, driver);
            
            // Publish trip accepted event
            publishTripEvent(trip, "TRIP_ACCEPTED");
            
            logger.info("Trip {} accepted by driver {}", tripId, driverId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error accepting trip {} by driver {}", tripId, driverId, e);
            return false;
        }
    }
    
    /**
     * Handle driver rejecting a trip
     */
    public void rejectTrip(String tripId, Long driverId, String reason) {
        logger.info("Driver {} rejected trip {} with reason: {}", driverId, tripId, reason);
        
        // Remove driver notification from Redis
        String notificationKey = "trip_notification:" + tripId + ":" + driverId;
        redisTemplate.delete(notificationKey);
        
        // Update driver's rejection count (for analytics)
        updateDriverRejectionStats(driverId);
        
        // Check if we need to find more drivers
        Optional<Trip> tripOpt = tripRepository.findByTripId(tripId);
        if (tripOpt.isPresent() && tripOpt.get().getStatus() == TripStatus.REQUESTED) {
            // Try to find more drivers if available
            expandSearchAndNotifyMoreDrivers(tripOpt.get());
        }
    }
    
    /**
     * Handle timeout for trip matching
     */
    private void setTripTimeout(Trip trip) {
        // Schedule timeout check
        String timeoutKey = "trip_timeout:" + trip.getTripId();
        redisTemplate.opsForValue().set(
            timeoutKey, 
            "timeout", 
            5, // 5 minutes timeout
            TimeUnit.MINUTES
        );
    }
    
    /**
     * Handle no drivers available scenario
     */
    private void handleNoDriversAvailable(Trip trip) {
        logger.warn("No drivers available for trip: {}", trip.getTripId());
        
        // Notify rider about no drivers
        notificationService.sendNoDriversAvailableToRider(trip);
        
        // Increase search radius and try again after delay
        scheduleExpandedSearch(trip);
    }
    
    /**
     * Schedule expanded search with larger radius
     */
    private void scheduleExpandedSearch(Trip trip) {
        // This would typically be handled by a scheduled job
        // For now, we'll just log it
        logger.info("Scheduling expanded search for trip: {}", trip.getTripId());
    }
    
    /**
     * Expand search radius and notify more drivers
     */
    private void expandSearchAndNotifyMoreDrivers(Trip trip) {
        // Increase search radius by 50%
        double expandedRadius = MAX_SEARCH_RADIUS_KM * 1.5;
        
        List<Driver> moreDrivers = findAvailableDrivers(
            trip.getPickupLatitude(),
            trip.getPickupLongitude(),
            trip.getRequestedVehicleType(),
            expandedRadius
        );
        
        if (!moreDrivers.isEmpty()) {
            List<Driver> sortedDrivers = sortDriversByOptimalCriteria(
                moreDrivers, 
                trip.getPickupLatitude(), 
                trip.getPickupLongitude()
            );
            notifyDriversSequentially(trip, sortedDrivers);
        }
    }
    
    /**
     * Cancel notifications to other drivers when trip is accepted
     */
    private void cancelOtherDriverNotifications(Trip trip, Driver acceptedDriver) {
        // This would cancel push notifications to other drivers
        // Implementation depends on notification service
        logger.info("Cancelling notifications to other drivers for trip: {}", trip.getTripId());
    }
    
    /**
     * Update driver rejection statistics
     */
    private void updateDriverRejectionStats(Long driverId) {
        // This would update driver's rejection count for analytics
        logger.debug("Updating rejection stats for driver: {}", driverId);
    }
    
    /**
     * Handle trip matching errors
     */
    private void handleTripMatchingError(Trip trip, Exception error) {
        logger.error("Trip matching error for trip: {}", trip.getTripId(), error);
        
        // Notify rider about system error
        notificationService.sendSystemErrorToRider(trip);
        
        // Mark trip as failed or retry based on error type
        // Implementation depends on error handling strategy
    }
    
    /**
     * Publish trip events to Kafka
     */
    private void publishTripEvent(Trip trip, String eventType) {
        try {
            TripEvent event = new TripEvent(
                trip.getTripId(),
                trip.getRider().getId(),
                trip.getDriver() != null ? trip.getDriver().getId() : null,
                eventType,
                LocalDateTime.now()
            );
            
            kafkaTemplate.send("trip-events", event);
            logger.debug("Published trip event: {} for trip: {}", eventType, trip.getTripId());
            
        } catch (Exception e) {
            logger.error("Error publishing trip event for trip: {}", trip.getTripId(), e);
        }
    }
    
    // Inner class for trip events
    public static class TripEvent {
        private String tripId;
        private Long riderId;
        private Long driverId;
        private String eventType;
        private LocalDateTime timestamp;
        
        public TripEvent(String tripId, Long riderId, Long driverId, String eventType, LocalDateTime timestamp) {
            this.tripId = tripId;
            this.riderId = riderId;
            this.driverId = driverId;
            this.eventType = eventType;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getTripId() { return tripId; }
        public void setTripId(String tripId) { this.tripId = tripId; }
        
        public Long getRiderId() { return riderId; }
        public void setRiderId(Long riderId) { this.riderId = riderId; }
        
        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}