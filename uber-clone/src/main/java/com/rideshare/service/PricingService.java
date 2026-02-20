package com.rideshare.service;

import com.rideshare.model.Trip;
import com.rideshare.model.VehicleType;
import com.rideshare.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Service for calculating trip pricing with dynamic surge pricing
 */
@Service
public class PricingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PricingService.class);
    
    // Base pricing configuration
    private static final BigDecimal BASE_FARE = new BigDecimal("50.00"); // Base fare in currency
    private static final BigDecimal PER_KM_RATE = new BigDecimal("12.00"); // Rate per kilometer
    private static final BigDecimal PER_MINUTE_RATE = new BigDecimal("2.00"); // Rate per minute
    private static final BigDecimal MINIMUM_FARE = new BigDecimal("80.00"); // Minimum fare
    private static final BigDecimal PLATFORM_COMMISSION_RATE = new BigDecimal("0.25"); // 25% commission
    
    // Surge pricing thresholds
    private static final double HIGH_DEMAND_THRESHOLD = 0.8; // 80% of drivers busy
    private static final double VERY_HIGH_DEMAND_THRESHOLD = 0.9; // 90% of drivers busy
    private static final BigDecimal MAX_SURGE_MULTIPLIER = new BigDecimal("3.0"); // Maximum 3x surge
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Calculate estimated fare for a trip
     */
    public BigDecimal calculateEstimatedFare(Trip trip) {
        try {
            // Get route information
            LocationService.RouteInfo routeInfo = locationService.getRouteInfo(
                trip.getPickupLatitude(), trip.getPickupLongitude(),
                trip.getDestinationLatitude(), trip.getDestinationLongitude()
            );
            
            // Set estimated distance and duration
            trip.setEstimatedDistanceKm(BigDecimal.valueOf(routeInfo.getDistanceKm()));
            trip.setEstimatedDurationMinutes(routeInfo.getDurationMinutes());
            
            // Calculate base fare components
            BigDecimal baseFare = getBaseFareForVehicleType(trip.getRequestedVehicleType());
            BigDecimal distanceFare = calculateDistanceFare(routeInfo.getDistanceKm(), trip.getRequestedVehicleType());
            BigDecimal timeFare = calculateTimeFare(routeInfo.getDurationMinutes(), trip.getRequestedVehicleType());
            
            // Calculate surge multiplier
            BigDecimal surgeMultiplier = calculateSurgeMultiplier(
                trip.getPickupLatitude(), 
                trip.getPickupLongitude()
            );
            
            // Calculate total before surge
            BigDecimal subtotal = baseFare.add(distanceFare).add(timeFare);
            
            // Apply minimum fare
            if (subtotal.compareTo(getMinimumFareForVehicleType(trip.getRequestedVehicleType())) < 0) {
                subtotal = getMinimumFareForVehicleType(trip.getRequestedVehicleType());
            }
            
            // Apply surge pricing
            BigDecimal surgeFare = subtotal.multiply(surgeMultiplier.subtract(BigDecimal.ONE));
            BigDecimal totalFare = subtotal.add(surgeFare);
            
            // Set fare components in trip
            trip.setBaseFare(baseFare);
            trip.setDistanceFare(distanceFare);
            trip.setTimeFare(timeFare);
            trip.setSurgeMultiplier(surgeMultiplier);
            trip.setSurgeFare(surgeFare);
            
            // Calculate driver earnings and platform commission
            BigDecimal platformCommission = totalFare.multiply(PLATFORM_COMMISSION_RATE);
            BigDecimal driverEarnings = totalFare.subtract(platformCommission);
            
            trip.setPlatformCommission(platformCommission);
            trip.setDriverEarnings(driverEarnings);
            
            logger.debug("Calculated fare for trip {}: Total={}, Surge={}x", 
                        trip.getTripId(), totalFare, surgeMultiplier);
            
            return totalFare.setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            logger.error("Error calculating fare for trip {}", trip.getTripId(), e);
            return getMinimumFareForVehicleType(trip.getRequestedVehicleType());
        }
    }
    
    /**
     * Calculate surge multiplier based on demand and supply
     */
    public BigDecimal calculateSurgeMultiplier(Double latitude, Double longitude) {
        try {
            String surgeKey = String.format("surge:%f,%f", 
                Math.round(latitude * 100.0) / 100.0, 
                Math.round(longitude * 100.0) / 100.0
            );
            
            // Check cached surge multiplier
            BigDecimal cachedSurge = (BigDecimal) redisTemplate.opsForValue().get(surgeKey);
            if (cachedSurge != null) {
                return cachedSurge;
            }
            
            // Calculate demand-supply ratio
            double demandSupplyRatio = calculateDemandSupplyRatio(latitude, longitude);
            
            // Calculate time-based multiplier
            BigDecimal timeMultiplier = calculateTimeBasedMultiplier();
            
            // Calculate weather-based multiplier (if weather service is available)
            BigDecimal weatherMultiplier = calculateWeatherBasedMultiplier(latitude, longitude);
            
            // Calculate event-based multiplier
            BigDecimal eventMultiplier = calculateEventBasedMultiplier(latitude, longitude);
            
            // Combine all factors
            BigDecimal surgeMultiplier = BigDecimal.ONE;
            
            // Apply demand-supply surge
            if (demandSupplyRatio > VERY_HIGH_DEMAND_THRESHOLD) {
                surgeMultiplier = surgeMultiplier.add(new BigDecimal("1.5")); // +150%
            } else if (demandSupplyRatio > HIGH_DEMAND_THRESHOLD) {
                surgeMultiplier = surgeMultiplier.add(new BigDecimal("0.5")); // +50%
            }
            
            // Apply time-based surge
            surgeMultiplier = surgeMultiplier.multiply(timeMultiplier);
            
            // Apply weather-based surge
            surgeMultiplier = surgeMultiplier.multiply(weatherMultiplier);
            
            // Apply event-based surge
            surgeMultiplier = surgeMultiplier.multiply(eventMultiplier);
            
            // Cap at maximum surge
            if (surgeMultiplier.compareTo(MAX_SURGE_MULTIPLIER) > 0) {
                surgeMultiplier = MAX_SURGE_MULTIPLIER;
            }
            
            // Ensure minimum of 1.0x
            if (surgeMultiplier.compareTo(BigDecimal.ONE) < 0) {
                surgeMultiplier = BigDecimal.ONE;
            }
            
            // Cache for 2 minutes
            redisTemplate.opsForValue().set(surgeKey, surgeMultiplier, 2, TimeUnit.MINUTES);
            
            logger.debug("Calculated surge multiplier for location {},{}: {}x", 
                        latitude, longitude, surgeMultiplier);
            
            return surgeMultiplier.setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            logger.error("Error calculating surge multiplier", e);
            return BigDecimal.ONE;
        }
    }
    
    /**
     * Calculate demand-supply ratio in the area
     */
    private double calculateDemandSupplyRatio(Double latitude, Double longitude) {
        try {
            // Count active trip requests in the area (demand)
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            long activeRequests = tripRepository.countActiveRequestsInArea(
                latitude, longitude, 2.0, fiveMinutesAgo // 2km radius
            );
            
            // Count available drivers in the area (supply)
            java.util.List<Long> nearbyDrivers = locationService.findNearbyDrivers(latitude, longitude, 2.0);
            long availableDrivers = nearbyDrivers.size();
            
            if (availableDrivers == 0) {
                return 1.0; // Maximum demand when no drivers available
            }
            
            return Math.min(1.0, (double) activeRequests / availableDrivers);
            
        } catch (Exception e) {
            logger.error("Error calculating demand-supply ratio", e);
            return 0.0;
        }
    }
    
    /**
     * Calculate time-based surge multiplier
     */
    private BigDecimal calculateTimeBasedMultiplier() {
        LocalTime now = LocalTime.now();
        
        // Peak hours: 7-10 AM and 5-8 PM
        if ((now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(10, 0))) ||
            (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(20, 0)))) {
            return new BigDecimal("1.2"); // 20% increase during peak hours
        }
        
        // Late night hours: 11 PM - 5 AM
        if (now.isAfter(LocalTime.of(23, 0)) || now.isBefore(LocalTime.of(5, 0))) {
            return new BigDecimal("1.3"); // 30% increase during late night
        }
        
        return BigDecimal.ONE;
    }
    
    /**
     * Calculate weather-based surge multiplier
     */
    private BigDecimal calculateWeatherBasedMultiplier(Double latitude, Double longitude) {
        // This would integrate with a weather service
        // For now, return base multiplier
        return BigDecimal.ONE;
    }
    
    /**
     * Calculate event-based surge multiplier
     */
    private BigDecimal calculateEventBasedMultiplier(Double latitude, Double longitude) {
        // This would check for special events in the area
        // For now, return base multiplier
        return BigDecimal.ONE;
    }
    
    /**
     * Get base fare for vehicle type
     */
    private BigDecimal getBaseFareForVehicleType(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> BASE_FARE;
            case COMFORT -> BASE_FARE.multiply(new BigDecimal("1.2"));
            case PREMIUM -> BASE_FARE.multiply(new BigDecimal("1.5"));
            case SUV -> BASE_FARE.multiply(new BigDecimal("1.8"));
            case LUXURY -> BASE_FARE.multiply(new BigDecimal("2.5"));
            case VAN -> BASE_FARE.multiply(new BigDecimal("2.0"));
        };
    }
    
    /**
     * Calculate distance-based fare
     */
    private BigDecimal calculateDistanceFare(double distanceKm, VehicleType vehicleType) {
        BigDecimal perKmRate = getPerKmRateForVehicleType(vehicleType);
        return perKmRate.multiply(BigDecimal.valueOf(distanceKm));
    }
    
    /**
     * Calculate time-based fare
     */
    private BigDecimal calculateTimeFare(int durationMinutes, VehicleType vehicleType) {
        BigDecimal perMinuteRate = getPerMinuteRateForVehicleType(vehicleType);
        return perMinuteRate.multiply(BigDecimal.valueOf(durationMinutes));
    }
    
    /**
     * Get per-kilometer rate for vehicle type
     */
    private BigDecimal getPerKmRateForVehicleType(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> PER_KM_RATE;
            case COMFORT -> PER_KM_RATE.multiply(new BigDecimal("1.2"));
            case PREMIUM -> PER_KM_RATE.multiply(new BigDecimal("1.5"));
            case SUV -> PER_KM_RATE.multiply(new BigDecimal("1.8"));
            case LUXURY -> PER_KM_RATE.multiply(new BigDecimal("2.5"));
            case VAN -> PER_KM_RATE.multiply(new BigDecimal("2.0"));
        };
    }
    
    /**
     * Get per-minute rate for vehicle type
     */
    private BigDecimal getPerMinuteRateForVehicleType(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> PER_MINUTE_RATE;
            case COMFORT -> PER_MINUTE_RATE.multiply(new BigDecimal("1.2"));
            case PREMIUM -> PER_MINUTE_RATE.multiply(new BigDecimal("1.5"));
            case SUV -> PER_MINUTE_RATE.multiply(new BigDecimal("1.8"));
            case LUXURY -> PER_MINUTE_RATE.multiply(new BigDecimal("2.5"));
            case VAN -> PER_MINUTE_RATE.multiply(new BigDecimal("2.0"));
        };
    }
    
    /**
     * Get minimum fare for vehicle type
     */
    private BigDecimal getMinimumFareForVehicleType(VehicleType vehicleType) {
        return switch (vehicleType) {
            case ECONOMY -> MINIMUM_FARE;
            case COMFORT -> MINIMUM_FARE.multiply(new BigDecimal("1.2"));
            case PREMIUM -> MINIMUM_FARE.multiply(new BigDecimal("1.5"));
            case SUV -> MINIMUM_FARE.multiply(new BigDecimal("1.8"));
            case LUXURY -> MINIMUM_FARE.multiply(new BigDecimal("2.5"));
            case VAN -> MINIMUM_FARE.multiply(new BigDecimal("2.0"));
        };
    }
    
    /**
     * Calculate cancellation fee based on trip status
     */
    public BigDecimal calculateCancellationFee(Trip trip) {
        if (trip.getAcceptedAt() == null) {
            return BigDecimal.ZERO; // No fee if driver hasn't accepted
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceAccepted = java.time.Duration.between(trip.getAcceptedAt(), now).toMinutes();
        
        if (minutesSinceAccepted < 5) {
            return BigDecimal.ZERO; // No fee within 5 minutes of acceptance
        }
        
        // Flat cancellation fee after 5 minutes
        BigDecimal cancellationFee = new BigDecimal("30.00");
        
        // Higher fee if driver has arrived
        if (trip.getDriverArrivedAt() != null) {
            cancellationFee = new BigDecimal("50.00");
        }
        
        return cancellationFee;
    }
    
    /**
     * Get current surge information for an area
     */
    public SurgeInfo getSurgeInfo(Double latitude, Double longitude) {
        BigDecimal surgeMultiplier = calculateSurgeMultiplier(latitude, longitude);
        
        String surgeLevel;
        if (surgeMultiplier.compareTo(new BigDecimal("2.0")) >= 0) {
            surgeLevel = "VERY_HIGH";
        } else if (surgeMultiplier.compareTo(new BigDecimal("1.5")) >= 0) {
            surgeLevel = "HIGH";
        } else if (surgeMultiplier.compareTo(new BigDecimal("1.2")) >= 0) {
            surgeLevel = "MODERATE";
        } else {
            surgeLevel = "NORMAL";
        }
        
        return new SurgeInfo(surgeMultiplier, surgeLevel);
    }
    
    // Inner class for surge information
    public static class SurgeInfo {
        private BigDecimal multiplier;
        private String level;
        
        public SurgeInfo(BigDecimal multiplier, String level) {
            this.multiplier = multiplier;
            this.level = level;
        }
        
        public BigDecimal getMultiplier() { return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
}