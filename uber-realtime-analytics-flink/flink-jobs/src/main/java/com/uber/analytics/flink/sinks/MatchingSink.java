package com.uber.analytics.flink.sinks;

import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.RideEvent;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sink for driver-rider matching results
 */
public class MatchingSink extends RichSinkFunction<Tuple2<RideEvent, DriverEvent>> {
    
    private static final Logger logger = LoggerFactory.getLogger(MatchingSink.class);
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        logger.info("MatchingSink initialized");
    }
    
    @Override
    public void invoke(Tuple2<RideEvent, DriverEvent> match, Context context) throws Exception {
        RideEvent ride = match.f0;
        DriverEvent driver = match.f1;
        
        // In a real implementation, this would:
        // 1. Send match result to Kafka topic
        // 2. Update database with match information
        // 3. Send notifications to rider and driver
        // 4. Update Pinot for analytics
        
        logger.info("MATCH FOUND: Ride {} matched with Driver {} - Distance: {:.2f}km, ETA: {}min", 
                   ride.getRideId(), 
                   driver.getDriverId(),
                   calculateDistance(ride, driver),
                   calculateETA(ride, driver));
        
        // Simulate sending to downstream systems
        sendToKafka(match);
        updateDatabase(match);
        sendNotifications(match);
        updateAnalytics(match);
    }
    
    private void sendToKafka(Tuple2<RideEvent, DriverEvent> match) {
        // Simulate Kafka producer
        logger.debug("Sending match result to Kafka topic: matching-results");
        
        // In real implementation:
        // kafkaProducer.send(new ProducerRecord<>("matching-results", matchResult));
    }
    
    private void updateDatabase(Tuple2<RideEvent, DriverEvent> match) {
        // Simulate database update
        logger.debug("Updating database with match result");
        
        // In real implementation:
        // - Update ride status to "MATCHED"
        // - Update driver status to "ASSIGNED"
        // - Create match record with timestamp
    }
    
    private void sendNotifications(Tuple2<RideEvent, DriverEvent> match) {
        // Simulate notification service
        logger.debug("Sending notifications to rider and driver");
        
        // In real implementation:
        // - Send push notification to rider with driver details
        // - Send notification to driver with pickup location
        // - Send SMS/email confirmations
    }
    
    private void updateAnalytics(Tuple2<RideEvent, DriverEvent> match) {
        // Simulate analytics update
        logger.debug("Updating analytics with match metrics");
        
        // In real implementation:
        // - Update match success rate metrics
        // - Record matching latency
        // - Update driver utilization metrics
        // - Send to Pinot for real-time analytics
    }
    
    private double calculateDistance(RideEvent ride, DriverEvent driver) {
        double rideLat = ride.getPickupLocation().getLatitude();
        double rideLng = ride.getPickupLocation().getLongitude();
        double driverLat = driver.getLocation().getLatitude();
        double driverLng = driver.getLocation().getLongitude();
        
        // Haversine formula
        double dLat = Math.toRadians(driverLat - rideLat);
        double dLng = Math.toRadians(driverLng - rideLng);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(rideLat)) * Math.cos(Math.toRadians(driverLat)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c; // Earth radius in km
    }
    
    private int calculateETA(RideEvent ride, DriverEvent driver) {
        double distance = calculateDistance(ride, driver);
        double averageSpeed = 30.0; // km/h average city speed
        return (int) Math.ceil((distance / averageSpeed) * 60); // minutes
    }
    
    @Override
    public void close() throws Exception {
        super.close();
        logger.info("MatchingSink closed");
    }
    
    /**
     * Sink for unmatched ride requests
     */
    public static class UnmatchedRequestsSink implements SinkFunction<RideEvent> {
        
        private static final Logger logger = LoggerFactory.getLogger(UnmatchedRequestsSink.class);
        
        @Override
        public void invoke(RideEvent unmatchedRide, Context context) throws Exception {
            logger.warn("UNMATCHED REQUEST: Ride {} could not be matched - Reason: No available drivers", 
                       unmatchedRide.getRideId());
            
            // In a real implementation, this would:
            // 1. Send to retry queue with exponential backoff
            // 2. Notify rider about delay
            // 3. Trigger surge pricing if pattern detected
            // 4. Update analytics with unmatched request metrics
            
            sendToRetryQueue(unmatchedRide);
            notifyRiderOfDelay(unmatchedRide);
            updateUnmatchedMetrics(unmatchedRide);
        }
        
        private void sendToRetryQueue(RideEvent ride) {
            logger.debug("Sending unmatched ride to retry queue: {}", ride.getRideId());
            // Implementation would send to Kafka retry topic
        }
        
        private void notifyRiderOfDelay(RideEvent ride) {
            logger.debug("Notifying rider of matching delay: {}", ride.getRiderId());
            // Implementation would send notification to rider
        }
        
        private void updateUnmatchedMetrics(RideEvent ride) {
            logger.debug("Updating unmatched request metrics");
            // Implementation would update analytics metrics
        }
    }
}