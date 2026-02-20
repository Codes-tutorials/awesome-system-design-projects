package com.uber.analytics.flink.sources;

import com.uber.analytics.events.DriverEvent;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;

/**
 * Kafka source for driver events
 */
public class DriverEventSource implements SourceFunction<DriverEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(DriverEventSource.class);
    private static final String KAFKA_TOPIC = "driver-events";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String CONSUMER_GROUP = "driver-events-consumer";
    
    private volatile boolean isRunning = true;
    private KafkaSource<DriverEvent> kafkaSource;
    private Random random = new Random();
    
    public DriverEventSource() {
        this.kafkaSource = createKafkaSource();
    }
    
    @Override
    public void run(SourceContext<DriverEvent> sourceContext) throws Exception {
        logger.info("Starting DriverEventSource");
        
        // For demonstration, we'll simulate driver events
        simulateDriverEvents(sourceContext);
    }
    
    @Override
    public void cancel() {
        logger.info("Cancelling DriverEventSource");
        isRunning = false;
    }
    
    private KafkaSource<DriverEvent> createKafkaSource() {
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        kafkaProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaProps.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        
        return KafkaSource.<DriverEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP_SERVERS)
                .setTopics(KAFKA_TOPIC)
                .setGroupId(CONSUMER_GROUP)
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(createAvroDeserializer())
                .setProperties(kafkaProps)
                .build();
    }
    
    private DeserializationSchema<DriverEvent> createAvroDeserializer() {
        return new DeserializationSchema<DriverEvent>() {
            @Override
            public DriverEvent deserialize(byte[] bytes) {
                // This would deserialize from Avro format
                return null;
            }
            
            @Override
            public boolean isEndOfStream(DriverEvent driverEvent) {
                return false;
            }
            
            @Override
            public TypeInformation<DriverEvent> getProducedType() {
                return TypeInformation.of(DriverEvent.class);
            }
        };
    }
    
    private void simulateDriverEvents(SourceContext<DriverEvent> sourceContext) throws InterruptedException {
        logger.info("Simulating driver events for demonstration");
        
        int eventCounter = 0;
        String[] vehicleTypes = {"SEDAN", "SUV", "HATCHBACK", "LUXURY"};
        String[] cities = {"san_francisco", "new_york", "london", "mumbai"};
        String[] statuses = {"AVAILABLE", "BUSY", "OFFLINE"};
        
        // Simulate 500 drivers
        int numDrivers = 500;
        DriverState[] drivers = new DriverState[numDrivers];
        
        // Initialize drivers
        for (int i = 0; i < numDrivers; i++) {
            drivers[i] = new DriverState(
                "driver_" + i,
                cities[i % cities.length],
                vehicleTypes[i % vehicleTypes.length]
            );
        }
        
        while (isRunning) {
            // Update random drivers
            int numUpdates = random.nextInt(50) + 10; // 10-60 updates per cycle
            
            for (int i = 0; i < numUpdates; i++) {
                int driverIndex = random.nextInt(numDrivers);
                DriverState driver = drivers[driverIndex];
                
                // Update driver location and status
                driver.updateLocation();
                driver.updateStatus();
                
                // Create driver event
                DriverEvent driverEvent = createDriverEvent(driver, eventCounter++);
                
                synchronized (sourceContext.getCheckpointLock()) {
                    sourceContext.collect(driverEvent);
                }
            }
            
            // Emit updates every 2 seconds
            Thread.sleep(2000);
            
            if (eventCounter % 1000 == 0) {
                logger.debug("Generated {} driver events", eventCounter);
            }
        }
        
        logger.info("DriverEventSource stopped after generating {} events", eventCounter);
    }
    
    private DriverEvent createDriverEvent(DriverState driver, int eventId) {
        return DriverEvent.builder()
                .driverId(driver.driverId)
                .eventType("LOCATION_UPDATE")
                .timestamp(System.currentTimeMillis())
                .location(new Location(driver.currentLat, driver.currentLng))
                .status(driver.status)
                .vehicleType(driver.vehicleType)
                .rating(driver.rating)
                .city(driver.city)
                .build();
    }
    
    // Helper class to maintain driver state
    private class DriverState {
        String driverId;
        String city;
        String vehicleType;
        String status;
        double baseLat, baseLng;
        double currentLat, currentLng;
        double rating;
        long lastStatusChange;
        
        public DriverState(String driverId, String city, String vehicleType) {
            this.driverId = driverId;
            this.city = city;
            this.vehicleType = vehicleType;
            this.status = "AVAILABLE";
            this.rating = 3.5 + random.nextDouble() * 1.5; // Rating between 3.5 and 5.0
            this.lastStatusChange = System.currentTimeMillis();
            
            // Set base coordinates for city
            switch (city) {
                case "san_francisco":
                    this.baseLat = 37.7749;
                    this.baseLng = -122.4194;
                    break;
                case "new_york":
                    this.baseLat = 40.7128;
                    this.baseLng = -74.0060;
                    break;
                case "london":
                    this.baseLat = 51.5074;
                    this.baseLng = -0.1278;
                    break;
                case "mumbai":
                    this.baseLat = 19.0760;
                    this.baseLng = 72.8777;
                    break;
                default:
                    this.baseLat = 37.7749;
                    this.baseLng = -122.4194;
            }
            
            // Initialize current location near base
            this.currentLat = baseLat + (random.nextDouble() - 0.5) * 0.2;
            this.currentLng = baseLng + (random.nextDouble() - 0.5) * 0.2;
        }
        
        public void updateLocation() {
            // Simulate driver movement
            if ("AVAILABLE".equals(status) || "BUSY".equals(status)) {
                // Small random movement
                double maxMovement = 0.001; // ~100m
                currentLat += (random.nextDouble() - 0.5) * maxMovement;
                currentLng += (random.nextDouble() - 0.5) * maxMovement;
                
                // Keep within city bounds
                double maxDistance = 0.1; // ~10km from city center
                if (Math.abs(currentLat - baseLat) > maxDistance) {
                    currentLat = baseLat + Math.signum(currentLat - baseLat) * maxDistance;
                }
                if (Math.abs(currentLng - baseLng) > maxDistance) {
                    currentLng = baseLng + Math.signum(currentLng - baseLng) * maxDistance;
                }
            }
        }
        
        public void updateStatus() {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastChange = currentTime - lastStatusChange;
            
            // Change status occasionally
            if (timeSinceLastChange > 30000) { // 30 seconds minimum
                if (random.nextDouble() < 0.1) { // 10% chance to change status
                    String[] possibleStatuses = {"AVAILABLE", "BUSY", "OFFLINE"};
                    String newStatus = possibleStatuses[random.nextInt(possibleStatuses.length)];
                    
                    // Bias towards AVAILABLE status
                    if (random.nextDouble() < 0.6) {
                        newStatus = "AVAILABLE";
                    }
                    
                    if (!newStatus.equals(status)) {
                        status = newStatus;
                        lastStatusChange = currentTime;
                    }
                }
            }
        }
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