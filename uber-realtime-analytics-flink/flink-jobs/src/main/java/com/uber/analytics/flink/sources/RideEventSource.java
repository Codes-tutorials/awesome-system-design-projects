package com.uber.analytics.flink.sources;

import com.uber.analytics.events.RideEvent;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Kafka source for ride events
 */
public class RideEventSource implements SourceFunction<RideEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(RideEventSource.class);
    private static final String KAFKA_TOPIC = "ride-events";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String CONSUMER_GROUP = "ride-events-consumer";
    
    private volatile boolean isRunning = true;
    private KafkaSource<RideEvent> kafkaSource;
    
    public RideEventSource() {
        this.kafkaSource = createKafkaSource();
    }
    
    @Override
    public void run(SourceContext<RideEvent> sourceContext) throws Exception {
        logger.info("Starting RideEventSource");
        
        // This is a simplified implementation
        // In a real Flink application, you would use the KafkaSource directly
        // without wrapping it in a SourceFunction
        
        // For demonstration, we'll simulate ride events
        simulateRideEvents(sourceContext);
    }
    
    @Override
    public void cancel() {
        logger.info("Cancelling RideEventSource");
        isRunning = false;
    }
    
    private KafkaSource<RideEvent> createKafkaSource() {
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        kafkaProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaProps.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        
        return KafkaSource.<RideEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(KAFKA_TOPIC)
                .setGroupId(CONSUMER_GROUP)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(createAvroDeserializer())
                .setProperties(kafkaProps)
                .build();
    }
    
    private DeserializationSchema<RideEvent> createAvroDeserializer() {
        // In a real implementation, you would use the generated Avro classes
        // For now, we'll use a simple deserializer
        return new DeserializationSchema<RideEvent>() {
            @Override
            public RideEvent deserialize(byte[] bytes) {
                // This would deserialize from Avro format
                // For demonstration, returning null (will be handled by simulation)
                return null;
            }
            
            @Override
            public boolean isEndOfStream(RideEvent rideEvent) {
                return false;
            }
            
            @Override
            public TypeInformation<RideEvent> getProducedType() {
                return TypeInformation.of(RideEvent.class);
            }
        };
    }
    
    private void simulateRideEvents(SourceContext<RideEvent> sourceContext) throws InterruptedException {
        logger.info("Simulating ride events for demonstration");
        
        int eventCounter = 0;
        String[] rideTypes = {"UBER_X", "UBER_XL", "UBER_BLACK", "UBER_POOL"};
        String[] cities = {"san_francisco", "new_york", "london", "mumbai"};
        
        while (isRunning) {
            // Create a simulated ride event
            RideEvent rideEvent = createSimulatedRideEvent(eventCounter++, rideTypes, cities);
            
            synchronized (sourceContext.getCheckpointLock()) {
                sourceContext.collect(rideEvent);
            }
            
            // Emit events every 100ms for demonstration
            Thread.sleep(100);
            
            if (eventCounter % 100 == 0) {
                logger.debug("Generated {} ride events", eventCounter);
            }
        }
        
        logger.info("RideEventSource stopped after generating {} events", eventCounter);
    }
    
    private RideEvent createSimulatedRideEvent(int eventId, String[] rideTypes, String[] cities) {
        String city = cities[eventId % cities.length];
        String rideType = rideTypes[eventId % rideTypes.length];
        
        // Base coordinates for different cities
        double baseLat, baseLng;
        switch (city) {
            case "san_francisco":
                baseLat = 37.7749;
                baseLng = -122.4194;
                break;
            case "new_york":
                baseLat = 40.7128;
                baseLng = -74.0060;
                break;
            case "london":
                baseLat = 51.5074;
                baseLng = -0.1278;
                break;
            case "mumbai":
                baseLat = 19.0760;
                baseLng = 72.8777;
                break;
            default:
                baseLat = 37.7749;
                baseLng = -122.4194;
        }
        
        // Add some randomness to coordinates
        double pickupLat = baseLat + (Math.random() - 0.5) * 0.1; // ~5km radius
        double pickupLng = baseLng + (Math.random() - 0.5) * 0.1;
        double dropoffLat = baseLat + (Math.random() - 0.5) * 0.2; // ~10km radius
        double dropoffLng = baseLng + (Math.random() - 0.5) * 0.2;
        
        return RideEvent.builder()
                .rideId("ride_" + eventId)
                .riderId("rider_" + (eventId % 1000))
                .eventType("RIDE_REQUESTED")
                .timestamp(System.currentTimeMillis())
                .pickupLocation(new Location(pickupLat, pickupLng))
                .dropoffLocation(new Location(dropoffLat, dropoffLng))
                .rideType(rideType)
                .estimatedFare(calculateEstimatedFare(pickupLat, pickupLng, dropoffLat, dropoffLng))
                .city(city)
                .build();
    }
    
    private double calculateEstimatedFare(double pickupLat, double pickupLng, double dropoffLat, double dropoffLng) {
        // Simple distance-based fare calculation
        double distance = calculateDistance(pickupLat, pickupLng, dropoffLat, dropoffLng);
        double baseFare = 2.50;
        double perKmRate = 1.20;
        return baseFare + (distance * perKmRate);
    }
    
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // Simplified distance calculation
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c; // Earth radius in km
    }
    
    // Helper class for location
    public static class Location {
        private final double latitude;
        private final double longitude;
        
        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}