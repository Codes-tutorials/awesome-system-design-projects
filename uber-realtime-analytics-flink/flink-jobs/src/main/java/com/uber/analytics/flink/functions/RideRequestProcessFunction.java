package com.uber.analytics.flink.functions;

import com.uber.analytics.events.RideEvent;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process function for ride requests
 * Validates and enriches ride request events before matching
 */
public class RideRequestProcessFunction extends ProcessFunction<RideEvent, RideEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(RideRequestProcessFunction.class);
    
    @Override
    public void processElement(RideEvent rideEvent, Context context, Collector<RideEvent> collector) throws Exception {
        
        // Validate ride request
        if (!isValidRideRequest(rideEvent)) {
            logger.warn("Invalid ride request received: {}", rideEvent.getRideId());
            return;
        }
        
        // Enrich ride request with additional context
        RideEvent enrichedEvent = enrichRideRequest(rideEvent, context);
        
        // Emit the processed ride request
        collector.collect(enrichedEvent);
        
        logger.debug("Processed ride request: {}", enrichedEvent.getRideId());
    }
    
    private boolean isValidRideRequest(RideEvent rideEvent) {
        // Basic validation checks
        if (rideEvent.getRideId() == null || rideEvent.getRideId().toString().isEmpty()) {
            return false;
        }
        
        if (rideEvent.getRiderId() == null || rideEvent.getRiderId().toString().isEmpty()) {
            return false;
        }
        
        if (rideEvent.getPickupLocation() == null || rideEvent.getDropoffLocation() == null) {
            return false;
        }
        
        // Validate coordinates
        double pickupLat = rideEvent.getPickupLocation().getLatitude();
        double pickupLng = rideEvent.getPickupLocation().getLongitude();
        double dropoffLat = rideEvent.getDropoffLocation().getLatitude();
        double dropoffLng = rideEvent.getDropoffLocation().getLongitude();
        
        if (!isValidCoordinate(pickupLat, pickupLng) || !isValidCoordinate(dropoffLat, dropoffLng)) {
            return false;
        }
        
        // Validate ride type
        if (rideEvent.getRideType() == null) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidCoordinate(double lat, double lng) {
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
    }
    
    private RideEvent enrichRideRequest(RideEvent rideEvent, Context context) {
        // Create a new enriched event (assuming RideEvent is immutable)
        // In a real implementation, you might add:
        // - Estimated trip duration
        // - Base fare calculation
        // - Pickup zone information
        // - Traffic conditions
        // - Weather information
        
        // For now, just return the original event with processing timestamp
        // In a real implementation, you would create a new event with enriched data
        
        return rideEvent;
    }
}