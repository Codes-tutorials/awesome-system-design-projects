package com.uber.analytics.flink.functions;

import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.RideEvent;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Driver matching function that matches ride requests with available drivers
 * Uses geospatial proximity and driver ratings for optimal matching
 */
public class DriverMatchingFunction extends CoProcessFunction<RideEvent, DriverEvent, Tuple2<RideEvent, DriverEvent>> {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverMatchingFunction.class);
    private static final double MAX_MATCHING_DISTANCE_KM = 5.0;
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    // State to store pending ride requests
    private transient MapState<String, RideEvent> pendingRideRequests;
    
    // State to store available drivers by location grid
    private transient MapState<String, List<DriverEvent>> availableDriversByGrid;
    
    // State to track driver availability
    private transient MapState<String, DriverEvent> driverAvailability;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        // Initialize state for pending ride requests
        MapStateDescriptor<String, RideEvent> rideRequestsDescriptor = 
            new MapStateDescriptor<>("pending-ride-requests", String.class, RideEvent.class);
        pendingRideRequests = getRuntimeContext().getMapState(rideRequestsDescriptor);
        
        // Initialize state for available drivers by grid
        MapStateDescriptor<String, List<DriverEvent>> driversGridDescriptor = 
            new MapStateDescriptor<>("drivers-by-grid", String.class, 
                getRuntimeContext().getTypeInformation(List.class));
        availableDriversByGrid = getRuntimeContext().getMapState(driversGridDescriptor);
        
        // Initialize state for driver availability
        MapStateDescriptor<String, DriverEvent> driverAvailabilityDescriptor = 
            new MapStateDescriptor<>("driver-availability", String.class, DriverEvent.class);
        driverAvailability = getRuntimeContext().getMapState(driverAvailabilityDescriptor);
        
        logger.info("DriverMatchingFunction initialized");
    }
    
    @Override
    public void processElement1(RideEvent rideRequest, Context context, 
                               Collector<Tuple2<RideEvent, DriverEvent>> collector) throws Exception {
        
        logger.debug("Processing ride request: {}", rideRequest.getRideId());
        
        // Store the ride request
        pendingRideRequests.put(rideRequest.getRideId().toString(), rideRequest);
        
        // Try to find a matching driver
        DriverEvent matchedDriver = findBestDriver(rideRequest);
        
        if (matchedDriver != null) {
            logger.info("Found match for ride {} with driver {}", 
                       rideRequest.getRideId(), matchedDriver.getDriverId());
            
            // Remove matched driver from availability
            driverAvailability.remove(matchedDriver.getDriverId().toString());
            
            // Remove the ride request from pending
            pendingRideRequests.remove(rideRequest.getRideId().toString());
            
            // Emit the match
            collector.collect(new Tuple2<>(rideRequest, matchedDriver));
        } else {
            logger.debug("No driver found for ride request: {}", rideRequest.getRideId());
            
            // Set a timer to retry matching after 30 seconds
            context.timerService().registerEventTimeTimer(context.timestamp() + 30000);
        }
    }
    
    @Override
    public void processElement2(DriverEvent driverEvent, Context context, 
                               Collector<Tuple2<RideEvent, DriverEvent>> collector) throws Exception {
        
        logger.debug("Processing driver event: {} - {}", 
                    driverEvent.getDriverId(), driverEvent.getStatus());
        
        String driverId = driverEvent.getDriverId().toString();
        
        if (driverEvent.getStatus().toString().equals("AVAILABLE")) {
            // Update driver availability
            driverAvailability.put(driverId, driverEvent);
            
            // Add driver to location grid
            String gridKey = getLocationGrid(driverEvent.getLocation().getLatitude(), 
                                           driverEvent.getLocation().getLongitude());
            List<DriverEvent> driversInGrid = availableDriversByGrid.get(gridKey);
            if (driversInGrid == null) {
                driversInGrid = new ArrayList<>();
            }
            
            // Remove driver if already in grid (update location)
            driversInGrid.removeIf(d -> d.getDriverId().equals(driverEvent.getDriverId()));
            driversInGrid.add(driverEvent);
            availableDriversByGrid.put(gridKey, driversInGrid);
            
            // Try to match with pending ride requests
            tryMatchWithPendingRequests(driverEvent, collector);
            
        } else {
            // Driver is not available, remove from state
            driverAvailability.remove(driverId);
            
            // Remove from all location grids
            removeDriverFromAllGrids(driverEvent);
        }
    }
    
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, 
                       Collector<Tuple2<RideEvent, DriverEvent>> collector) throws Exception {
        
        // Retry matching for pending requests
        for (RideEvent pendingRequest : pendingRideRequests.values()) {
            DriverEvent matchedDriver = findBestDriver(pendingRequest);
            
            if (matchedDriver != null) {
                logger.info("Timer-based match found for ride {} with driver {}", 
                           pendingRequest.getRideId(), matchedDriver.getDriverId());
                
                // Remove matched driver from availability
                driverAvailability.remove(matchedDriver.getDriverId().toString());
                
                // Remove the ride request from pending
                pendingRideRequests.remove(pendingRequest.getRideId().toString());
                
                // Emit the match
                collector.collect(new Tuple2<>(pendingRequest, matchedDriver));
            }
        }
        
        // Clean up old pending requests (older than 10 minutes)
        long cutoffTime = timestamp - 600000; // 10 minutes
        List<String> expiredRequests = new ArrayList<>();
        
        for (RideEvent request : pendingRideRequests.values()) {
            if (request.getTimestamp() < cutoffTime) {
                expiredRequests.add(request.getRideId().toString());
            }
        }
        
        for (String expiredRequestId : expiredRequests) {
            RideEvent expiredRequest = pendingRideRequests.get(expiredRequestId);
            pendingRideRequests.remove(expiredRequestId);
            
            // Emit as unmatched
            collector.collect(new Tuple2<>(expiredRequest, null));
            logger.warn("Ride request expired: {}", expiredRequestId);
        }
    }
    
    private DriverEvent findBestDriver(RideEvent rideRequest) throws Exception {
        double pickupLat = rideRequest.getPickupLocation().getLatitude();
        double pickupLng = rideRequest.getPickupLocation().getLongitude();
        
        DriverEvent bestDriver = null;
        double bestScore = Double.MAX_VALUE;
        
        // Search in nearby grids
        List<String> nearbyGrids = getNearbyGrids(pickupLat, pickupLng);
        
        for (String gridKey : nearbyGrids) {
            List<DriverEvent> driversInGrid = availableDriversByGrid.get(gridKey);
            if (driversInGrid == null) continue;
            
            for (DriverEvent driver : driversInGrid) {
                double distance = calculateDistance(
                    pickupLat, pickupLng,
                    driver.getLocation().getLatitude(),
                    driver.getLocation().getLongitude()
                );
                
                if (distance <= MAX_MATCHING_DISTANCE_KM) {
                    // Calculate matching score (lower is better)
                    double score = calculateMatchingScore(rideRequest, driver, distance);
                    
                    if (score < bestScore) {
                        bestScore = score;
                        bestDriver = driver;
                    }
                }
            }
        }
        
        return bestDriver;
    }
    
    private double calculateMatchingScore(RideEvent rideRequest, DriverEvent driver, double distance) {
        // Scoring factors:
        // 1. Distance (weight: 0.6)
        // 2. Driver rating (weight: 0.3)
        // 3. Vehicle type match (weight: 0.1)
        
        double distanceScore = distance / MAX_MATCHING_DISTANCE_KM; // Normalize to 0-1
        double ratingScore = (5.0 - driver.getRating()) / 5.0; // Invert rating (lower is better)
        double vehicleScore = rideRequest.getRideType().equals(driver.getVehicleType()) ? 0.0 : 0.5;
        
        return (distanceScore * 0.6) + (ratingScore * 0.3) + (vehicleScore * 0.1);
    }
    
    private void tryMatchWithPendingRequests(DriverEvent driver, 
                                           Collector<Tuple2<RideEvent, DriverEvent>> collector) throws Exception {
        
        for (RideEvent pendingRequest : pendingRideRequests.values()) {
            double distance = calculateDistance(
                pendingRequest.getPickupLocation().getLatitude(),
                pendingRequest.getPickupLocation().getLongitude(),
                driver.getLocation().getLatitude(),
                driver.getLocation().getLongitude()
            );
            
            if (distance <= MAX_MATCHING_DISTANCE_KM) {
                logger.info("Immediate match found for ride {} with new driver {}", 
                           pendingRequest.getRideId(), driver.getDriverId());
                
                // Remove the ride request from pending
                pendingRideRequests.remove(pendingRequest.getRideId().toString());
                
                // Remove driver from availability (will be handled by caller)
                
                // Emit the match
                collector.collect(new Tuple2<>(pendingRequest, driver));
                return; // Driver can only match one ride
            }
        }
    }
    
    private void removeDriverFromAllGrids(DriverEvent driver) throws Exception {
        // This is inefficient but necessary for cleanup
        // In production, consider using a more efficient data structure
        for (String gridKey : availableDriversByGrid.keys()) {
            List<DriverEvent> driversInGrid = availableDriversByGrid.get(gridKey);
            if (driversInGrid != null) {
                driversInGrid.removeIf(d -> d.getDriverId().equals(driver.getDriverId()));
                if (driversInGrid.isEmpty()) {
                    availableDriversByGrid.remove(gridKey);
                } else {
                    availableDriversByGrid.put(gridKey, driversInGrid);
                }
            }
        }
    }
    
    private String getLocationGrid(double lat, double lng) {
        // Simple grid system: divide world into 0.01 degree squares (~1km)
        int latGrid = (int) Math.floor(lat * 100);
        int lngGrid = (int) Math.floor(lng * 100);
        return latGrid + "," + lngGrid;
    }
    
    private List<String> getNearbyGrids(double lat, double lng) {
        List<String> grids = new ArrayList<>();
        int latGrid = (int) Math.floor(lat * 100);
        int lngGrid = (int) Math.floor(lng * 100);
        
        // Include current grid and 8 surrounding grids
        for (int latOffset = -1; latOffset <= 1; latOffset++) {
            for (int lngOffset = -1; lngOffset <= 1; lngOffset++) {
                grids.add((latGrid + latOffset) + "," + (lngGrid + lngOffset));
            }
        }
        
        return grids;
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Haversine formula for calculating distance between two points
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}