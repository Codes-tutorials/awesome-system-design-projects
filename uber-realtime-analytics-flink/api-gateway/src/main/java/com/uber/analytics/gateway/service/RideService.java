package com.uber.analytics.gateway.service;

import com.uber.analytics.gateway.dto.RideRequestDto;
import com.uber.analytics.gateway.dto.RideResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for ride operations
 */
@Service
public class RideService {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private PricingService pricingService;
    
    private static final String RIDE_EVENTS_TOPIC = "ride-events";
    
    public RideResponseDto requestRide(RideRequestDto request) {
        // Generate ride ID
        String rideId = UUID.randomUUID().toString();
        
        // Get current pricing
        double surgeMultiplier = pricingService.getCurrentSurgeMultiplier(
            request.getPickupLat(), request.getPickupLng());
        
        // Calculate fare
        double estimatedFare = calculateFare(
            request.getPickupLat(), request.getPickupLng(),
            request.getDropoffLat(), request.getDropoffLng(),
            request.getRideType(), surgeMultiplier);
        
        // Create ride event
        RideEvent rideEvent = RideEvent.builder()
            .rideId(rideId)
            .riderId(request.getRiderId())
            .eventType("RIDE_REQUESTED")
            .timestamp(System.currentTimeMillis())
            .pickupLocation(new Location(request.getPickupLat(), request.getPickupLng()))
            .dropoffLocation(new Location(request.getDropoffLat(), request.getDropoffLng()))
            .rideType(request.getRideType())
            .estimatedFare(estimatedFare)
            .surgeMultiplier(surgeMultiplier)
            .build();
        
        // Send to Kafka
        kafkaTemplate.send(RIDE_EVENTS_TOPIC, rideId, rideEvent);
        
        // Return response
        return RideResponseDto.builder()
            .rideId(rideId)
            .status("REQUESTED")
            .estimatedFare(estimatedFare)
            .surgeMultiplier(surgeMultiplier)
            .estimatedArrival(calculateETA(request.getPickupLat(), request.getPickupLng()))
            .build();
    }
    
    public RideResponseDto getRide(String rideId) {
        // In a real implementation, this would query the database
        // For demo, return a mock response
        return RideResponseDto.builder()
            .rideId(rideId)
            .status("IN_PROGRESS")
            .build();
    }
    
    public List<RideResponseDto> getRidesByRider(String riderId) {
        // Mock implementation
        return List.of();
    }
    
    public RideResponseDto cancelRide(String rideId) {
        // Send cancellation event
        RideEvent cancelEvent = RideEvent.builder()
            .rideId(rideId)
            .eventType("RIDE_CANCELLED")
            .timestamp(System.currentTimeMillis())
            .build();
        
        kafkaTemplate.send(RIDE_EVENTS_TOPIC, rideId, cancelEvent);
        
        return RideResponseDto.builder()
            .rideId(rideId)
            .status("CANCELLED")
            .build();
    }
    
    public RideResponseDto completeRide(String rideId) {
        // Send completion event
        RideEvent completeEvent = RideEvent.builder()
            .rideId(rideId)
            .eventType("RIDE_COMPLETED")
            .timestamp(System.currentTimeMillis())
            .build();
        
        kafkaTemplate.send(RIDE_EVENTS_TOPIC, rideId, completeEvent);
        
        return RideResponseDto.builder()
            .rideId(rideId)
            .status("COMPLETED")
            .build();
    }
    
    public double estimateFare(double pickupLat, double pickupLng, 
                              double dropoffLat, double dropoffLng, String rideType) {
        double surgeMultiplier = pricingService.getCurrentSurgeMultiplier(pickupLat, pickupLng);
        return calculateFare(pickupLat, pickupLng, dropoffLat, dropoffLng, rideType, surgeMultiplier);
    }
    
    private double calculateFare(double pickupLat, double pickupLng, 
                                double dropoffLat, double dropoffLng, 
                                String rideType, double surgeMultiplier) {
        
        double distance = calculateDistance(pickupLat, pickupLng, dropoffLat, dropoffLng);
        double baseFare = getBaseFare(rideType);
        double perKmRate = getPerKmRate(rideType);
        
        double fare = (baseFare + (distance * perKmRate)) * surgeMultiplier;
        return Math.round(fare * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Haversine formula
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c; // Earth radius in km
    }
    
    private double getBaseFare(String rideType) {
        switch (rideType) {
            case "UBER_X": return 2.50;
            case "UBER_XL": return 3.50;
            case "UBER_BLACK": return 5.00;
            case "UBER_POOL": return 1.50;
            default: return 2.50;
        }
    }
    
    private double getPerKmRate(String rideType) {
        switch (rideType) {
            case "UBER_X": return 1.20;
            case "UBER_XL": return 1.80;
            case "UBER_BLACK": return 2.50;
            case "UBER_POOL": return 0.80;
            default: return 1.20;
        }
    }
    
    private int calculateETA(double pickupLat, double pickupLng) {
        // Simplified ETA calculation
        // In reality, this would consider traffic, driver locations, etc.
        return 5 + (int)(Math.random() * 10); // 5-15 minutes
    }
    
    // Helper classes
    public static class RideEvent {
        // Implementation would match the Avro schema
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder rideId(String rideId) { return this; }
            public Builder riderId(String riderId) { return this; }
            public Builder eventType(String eventType) { return this; }
            public Builder timestamp(long timestamp) { return this; }
            public Builder pickupLocation(Location location) { return this; }
            public Builder dropoffLocation(Location location) { return this; }
            public Builder rideType(String rideType) { return this; }
            public Builder estimatedFare(double fare) { return this; }
            public Builder surgeMultiplier(double multiplier) { return this; }
            public RideEvent build() { return new RideEvent(); }
        }
    }
    
    public static class Location {
        public Location(double lat, double lng) {}
    }
}