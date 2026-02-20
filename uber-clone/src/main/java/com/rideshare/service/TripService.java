package com.rideshare.service;

import com.rideshare.dto.*;
import com.rideshare.model.*;
import com.rideshare.repository.DriverRepository;
import com.rideshare.repository.TripRepository;
import com.rideshare.repository.UserRepository;
import com.rideshare.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for trip management operations
 */
@Service
@Transactional
public class TripService {
    
    private static final Logger logger = LoggerFactory.getLogger(TripService.class);
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private TripMatchingService tripMatchingService;
    
    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Request a new trip
     */
    public Trip requestTrip(TripRequestDto request, User rider) {
        logger.info("Processing trip request for rider: {}", rider.getId());
        
        // Validate rider doesn't have active trip
        if (hasActiveTrip(rider)) {
            throw new IllegalStateException("Rider already has an active trip");
        }
        
        // Create new trip
        Trip trip = new Trip(
            rider,
            request.getVehicleType(),
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getPickupAddress(),
            request.getDestinationLatitude(),
            request.getDestinationLongitude(),
            request.getDestinationAddress()
        );
        
        // Generate unique trip ID
        trip.setTripId(generateTripId());
        
        // Set optional fields
        trip.setPickupLandmark(request.getPickupLandmark());
        trip.setDestinationLandmark(request.getDestinationLandmark());
        trip.setSpecialInstructions(request.getSpecialInstructions());
        
        // Calculate route information
        LocationService.RouteInfo routeInfo = locationService.calculateRoute(
            request.getPickupLatitude(), request.getPickupLongitude(),
            request.getDestinationLatitude(), request.getDestinationLongitude()
        );
        
        trip.setEstimatedDistanceKm(routeInfo.getDistanceKm());
        trip.setEstimatedDurationMinutes(routeInfo.getDurationMinutes());
        
        // Calculate estimated fare
        BigDecimal estimatedFare = pricingService.calculateEstimatedFare(trip);
        trip.setTotalFare(estimatedFare);
        
        // Save trip
        trip = tripRepository.save(trip);
        
        // Find and notify drivers
        tripMatchingService.findAndNotifyDrivers(trip);
        
        logger.info("Trip requested successfully: {}", trip.getTripId());
        return trip;
    }
    
    /**
     * Estimate fare for a trip
     */
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        logger.debug("Calculating fare estimate for route");
        
        // Calculate route information
        LocationService.RouteInfo routeInfo = locationService.calculateRoute(
            request.getPickupLatitude(), request.getPickupLongitude(),
            request.getDestinationLatitude(), request.getDestinationLongitude()
        );
        
        // Get surge information
        PricingService.SurgeInfo surgeInfo = pricingService.getSurgeInfo(
            request.getPickupLatitude(), request.getPickupLongitude()
        );
        
        // Calculate fare for different vehicle types
        List<FareEstimateResponse.FareOption> fareOptions = List.of(
            calculateFareOption(VehicleType.ECONOMY, routeInfo, surgeInfo),
            calculateFareOption(VehicleType.COMFORT, routeInfo, surgeInfo),
            calculateFareOption(VehicleType.PREMIUM, routeInfo, surgeInfo)
        );
        
        return new FareEstimateResponse(
            routeInfo.getDistanceKm(),
            routeInfo.getDurationMinutes(),
            fareOptions,
            new FareEstimateResponse.SurgeInfo(
                surgeInfo.getMultiplier(),
                surgeInfo.getLevel(),
                surgeInfo.getMessage()
            )
        );
    }
    
    private FareEstimateResponse.FareOption calculateFareOption(VehicleType vehicleType, 
                                                               LocationService.RouteInfo routeInfo,
                                                               PricingService.SurgeInfo surgeInfo) {
        
        PricingService.FareBreakdown breakdown = pricingService.calculateFareBreakdown(
            vehicleType, routeInfo.getDistanceKm(), routeInfo.getDurationMinutes(), surgeInfo.getMultiplier()
        );
        
        return new FareEstimateResponse.FareOption(
            vehicleType,
            getVehicleTypeDisplayName(vehicleType),
            breakdown.getBaseFare(),
            breakdown.getDistanceFare(),
            breakdown.getTimeFare(),
            breakdown.getSurgeFare(),
            breakdown.getTotalFare(),
            surgeInfo.getMultiplier(),
            calculateEstimatedArrival(vehicleType),
            getVehicleTypeDescription(vehicleType)
        );
    }
    
    /**
     * Get current active trip for user
     */
    public Trip getCurrentTrip(User user) {
        if (user.getRole() == UserRole.RIDER) {
            return tripRepository.findCurrentTripByRider(user).orElse(null);
        } else if (user.getRole() == UserRole.DRIVER) {
            Optional<Driver> driverOpt = driverRepository.findByUserId(user.getId());
            if (driverOpt.isPresent()) {
                return tripRepository.findCurrentTripByDriver(driverOpt.get()).orElse(null);
            }
        }
        return null;
    }
    
    /**
     * Get trip details
     */
    public Trip getTripDetails(String tripId, User user) {
        Trip trip = tripRepository.findByTripId(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        
        // Verify user has access to this trip
        if (!hasAccessToTrip(trip, user)) {
            throw new SecurityException("Access denied to trip");
        }
        
        return trip;
    }
    
    /**
     * Cancel a trip
     */
    public Trip cancelTrip(String tripId, User user, String reason) {
        Trip trip = getTripDetails(tripId, user);
        
        if (!trip.canBeCancelled()) {
            throw new IllegalStateException("Trip cannot be cancelled in current status: " + trip.getStatus());
        }
        
        CancelledBy cancelledBy = (user.getRole() == UserRole.RIDER) ? CancelledBy.RIDER : CancelledBy.DRIVER;
        
        // Calculate cancellation fee if applicable
        BigDecimal cancellationFee = calculateCancellationFee(trip, cancelledBy);
        trip.setCancellationFee(cancellationFee);
        
        // Cancel the trip
        trip.cancelTrip(cancelledBy, reason);
        
        // Update driver availability if driver was assigned
        if (trip.getDriver() != null) {
            Driver driver = trip.getDriver();
            driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
            driverRepository.save(driver);
        }
        
        // Save trip
        trip = tripRepository.save(trip);
        
        // Send notifications
        if (cancelledBy == CancelledBy.RIDER && trip.getDriver() != null) {
            notificationService.sendTripCancelledToDriver(trip);
        } else if (cancelledBy == CancelledBy.DRIVER) {
            notificationService.sendTripCancelledToRider(trip);
        }
        
        logger.info("Trip {} cancelled by {}", tripId, cancelledBy);
        return trip;
    }
    
    /**
     * Rate a completed trip
     */
    public void rateTrip(String tripId, User user, Integer rating, String feedback) {
        Trip trip = getTripDetails(tripId, user);
        
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new IllegalStateException("Can only rate completed trips");
        }
        
        if (user.getRole() == UserRole.RIDER) {
            trip.setRiderRating(rating);
            trip.setRiderFeedback(feedback);
            
            // Update driver's average rating
            if (trip.getDriver() != null) {
                updateDriverRating(trip.getDriver(), rating);
            }
        } else if (user.getRole() == UserRole.DRIVER) {
            trip.setDriverRating(rating);
            trip.setDriverFeedback(feedback);
            
            // Update rider's rating if needed
            updateRiderRating(trip.getRider(), rating);
        }
        
        tripRepository.save(trip);
        logger.info("Trip {} rated by {}: {} stars", tripId, user.getRole(), rating);
    }
    
    /**
     * Get trip history for user
     */
    public Page<TripHistoryResponse> getTripHistory(User user, Pageable pageable) {
        Page<Trip> trips;
        
        if (user.getRole() == UserRole.RIDER) {
            trips = tripRepository.findCompletedTripsByRider(user, pageable);
        } else if (user.getRole() == UserRole.DRIVER) {
            Optional<Driver> driverOpt = driverRepository.findByUserId(user.getId());
            if (driverOpt.isEmpty()) {
                throw new IllegalArgumentException("Driver profile not found");
            }
            trips = tripRepository.findCompletedTripsByDriver(driverOpt.get(), pageable);
        } else {
            throw new IllegalArgumentException("Invalid user role for trip history");
        }
        
        return trips.map(this::convertToTripHistoryResponse);
    }
    
    // Driver-specific methods
    
    /**
     * Accept a trip (Driver)
     */
    public Trip acceptTrip(String tripId, User driverUser) {
        Optional<Driver> driverOpt = driverRepository.findByUserId(driverUser.getId());
        if (driverOpt.isEmpty()) {
            throw new IllegalArgumentException("Driver profile not found");
        }
        
        Driver driver = driverOpt.get();
        
        // Use trip matching service to handle acceptance
        boolean accepted = tripMatchingService.acceptTrip(tripId, driver.getId());
        
        if (!accepted) {
            throw new IllegalStateException("Failed to accept trip");
        }
        
        return tripRepository.findByTripId(tripId)
            .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
    }
    
    /**
     * Reject a trip (Driver)
     */
    public void rejectTrip(String tripId, User driverUser, String reason) {
        Optional<Driver> driverOpt = driverRepository.findByUserId(driverUser.getId());
        if (driverOpt.isEmpty()) {
            throw new IllegalArgumentException("Driver profile not found");
        }
        
        Driver driver = driverOpt.get();
        tripMatchingService.rejectTrip(tripId, driver.getId(), reason);
    }
    
    /**
     * Mark driver as arrived
     */
    public Trip markDriverArrived(String tripId, User driverUser) {
        Trip trip = getTripDetails(tripId, driverUser);
        
        if (trip.getStatus() != TripStatus.ACCEPTED) {
            throw new IllegalStateException("Invalid trip status for driver arrival");
        }
        
        trip.setStatus(TripStatus.DRIVER_ARRIVED);
        trip.setDriverArrivedAt(LocalDateTime.now());
        
        trip = tripRepository.save(trip);
        
        // Notify rider
        notificationService.sendDriverArrivedToRider(trip);
        
        logger.info("Driver arrived for trip: {}", tripId);
        return trip;
    }
    
    /**
     * Start trip
     */
    public Trip startTrip(String tripId, User driverUser) {
        Trip trip = getTripDetails(tripId, driverUser);
        
        if (trip.getStatus() != TripStatus.DRIVER_ARRIVED) {
            throw new IllegalStateException("Invalid trip status for trip start");
        }
        
        trip.startTrip();
        trip = tripRepository.save(trip);
        
        // Notify rider
        notificationService.sendTripStartedToRider(trip);
        
        logger.info("Trip started: {}", tripId);
        return trip;
    }
    
    /**
     * Complete trip
     */
    public Trip completeTrip(String tripId, User driverUser, TripCompletionRequest request) {
        Trip trip = getTripDetails(tripId, driverUser);
        
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new IllegalStateException("Invalid trip status for trip completion");
        }
        
        // Calculate actual duration
        if (trip.getTripStartedAt() != null) {
            long actualMinutes = Duration.between(trip.getTripStartedAt(), LocalDateTime.now()).toMinutes();
            trip.setActualDurationMinutes((int) actualMinutes);
        }
        
        // Set actual distance and final fare
        trip.setActualDistanceKm(request.getActualDistanceKm());
        
        // Recalculate final fare based on actual distance and time
        BigDecimal finalFare = pricingService.calculateFinalFare(trip);
        trip.setTotalFare(finalFare);
        
        // Complete the trip
        trip.completeTrip();
        
        // Update driver availability
        Driver driver = trip.getDriver();
        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        driver.setCompletedTrips(driver.getCompletedTrips() + 1);
        driverRepository.save(driver);
        
        trip = tripRepository.save(trip);
        
        // Notify rider
        notificationService.sendTripCompletedToRider(trip);
        
        logger.info("Trip completed: {}", tripId);
        return trip;
    }
    
    /**
     * Update trip location during ride
     */
    public void updateTripLocation(String tripId, User driverUser, LocationUpdateRequest request) {
        Trip trip = getTripDetails(tripId, driverUser);
        
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            return; // Only update location during active trip
        }
        
        // Update driver location
        Optional<Driver> driverOpt = driverRepository.findByUserId(driverUser.getId());
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setCurrentLatitude(request.getLatitude());
            driver.setCurrentLongitude(request.getLongitude());
            driver.setLocationUpdatedAt(LocalDateTime.now());
            driverRepository.save(driver);
            
            // Send real-time location update to rider
            notificationService.sendLocationUpdateToRider(trip, request);
        }
    }
    
    // Helper methods
    
    private boolean hasActiveTrip(User rider) {
        return tripRepository.existsByRiderAndStatusIn(
            rider, 
            List.of(TripStatus.REQUESTED, TripStatus.ACCEPTED, TripStatus.DRIVER_ARRIVED, TripStatus.IN_PROGRESS)
        );
    }
    
    private boolean hasAccessToTrip(Trip trip, User user) {
        if (user.getRole() == UserRole.RIDER) {
            return trip.getRider().getId().equals(user.getId());
        } else if (user.getRole() == UserRole.DRIVER) {
            return trip.getDriver() != null && trip.getDriver().getUser().getId().equals(user.getId());
        }
        return false;
    }
    
    private String generateTripId() {
        return "TRIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private BigDecimal calculateCancellationFee(Trip trip, CancelledBy cancelledBy) {
        // Implement cancellation fee logic based on trip status and timing
        if (cancelledBy == CancelledBy.RIDER && trip.getStatus() == TripStatus.DRIVER_ARRIVED) {
            return new BigDecimal("50.00"); // Fixed cancellation fee
        }
        return BigDecimal.ZERO;
    }
    
    private void updateDriverRating(Driver driver, Integer newRating) {
        // Calculate new average rating
        BigDecimal currentRating = driver.getAverageRating() != null ? driver.getAverageRating() : BigDecimal.ZERO;
        int totalRatings = driver.getTotalRatings() != null ? driver.getTotalRatings() : 0;
        
        BigDecimal totalScore = currentRating.multiply(new BigDecimal(totalRatings));
        totalScore = totalScore.add(new BigDecimal(newRating));
        totalRatings++;
        
        BigDecimal newAverageRating = totalScore.divide(new BigDecimal(totalRatings), 2, BigDecimal.ROUND_HALF_UP);
        
        driver.setAverageRating(newAverageRating);
        driver.setTotalRatings(totalRatings);
        driverRepository.save(driver);
    }
    
    private void updateRiderRating(User rider, Integer newRating) {
        // Similar logic for rider rating if needed
        logger.debug("Updating rider rating for user: {}", rider.getId());
    }
    
    private TripHistoryResponse convertToTripHistoryResponse(Trip trip) {
        TripHistoryResponse response = new TripHistoryResponse();
        response.setTripId(trip.getTripId());
        response.setStatus(trip.getStatus());
        response.setVehicleType(trip.getRequestedVehicleType());
        response.setPickupAddress(trip.getPickupAddress());
        response.setDestinationAddress(trip.getDestinationAddress());
        response.setTotalFare(trip.getTotalFare());
        response.setActualDistanceKm(trip.getActualDistanceKm());
        response.setActualDurationMinutes(trip.getActualDurationMinutes());
        response.setRequestedAt(trip.getRequestedAt());
        response.setCompletedAt(trip.getTripCompletedAt());
        
        if (trip.getDriver() != null) {
            response.setDriverName(trip.getDriver().getUser().getFirstName() + " " + 
                                 trip.getDriver().getUser().getLastName());
            
            if (trip.getVehicle() != null) {
                response.setVehicleInfo(trip.getVehicle().getMake() + " " + 
                                      trip.getVehicle().getModel() + " - " + 
                                      trip.getVehicle().getLicensePlate());
            }
        }
        
        return response;
    }
    
    private String getVehicleTypeDisplayName(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> "Economy";
            case COMFORT -> "Comfort";
            case PREMIUM -> "Premium";
            case LUXURY -> "Luxury";
            case SUV -> "SUV";
        };
    }
    
    private String getVehicleTypeDescription(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> "Affordable rides for everyday trips";
            case COMFORT -> "More space and comfort for your journey";
            case PREMIUM -> "Premium vehicles with top-rated drivers";
            case LUXURY -> "Luxury vehicles for special occasions";
            case SUV -> "Extra space for groups and luggage";
        };
    }
    
    private Integer calculateEstimatedArrival(VehicleType vehicleType) {
        // Mock implementation - in reality, this would be based on driver availability
        return switch (vehicleType) {
            case ECONOMY -> 5;
            case COMFORT -> 7;
            case PREMIUM -> 10;
            case LUXURY -> 15;
            case SUV -> 8;
        };
    }
}