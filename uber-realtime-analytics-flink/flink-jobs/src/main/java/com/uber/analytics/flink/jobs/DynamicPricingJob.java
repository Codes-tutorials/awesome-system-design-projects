package com.uber.analytics.flink.jobs;

import com.uber.analytics.events.RideEvent;
import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.flink.functions.DemandSupplyCalculator;
import com.uber.analytics.flink.functions.SurgePricingFunction;
import com.uber.analytics.flink.model.PricingUpdate;
import com.uber.analytics.flink.model.DemandSupplyMetrics;
import com.uber.analytics.flink.sinks.PricingSink;
import com.uber.analytics.flink.sources.RideEventSource;
import com.uber.analytics.flink.sources.DriverEventSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Dynamic pricing job that calculates surge pricing based on real-time demand and supply
 */
public class DynamicPricingJob {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicPricingJob.class);
    
    public void execute(StreamExecutionEnvironment env) {
        logger.info("Configuring Dynamic Pricing Job");
        
        // Ride demand stream
        DataStream<RideEvent> rideStream = env
                .addSource(new RideEventSource())
                .name("ride-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<RideEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
        
        // Driver supply stream
        DataStream<DriverEvent> driverStream = env
                .addSource(new DriverEventSource())
                .name("driver-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<DriverEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
        
        // Calculate demand metrics by location (using geohash for partitioning)
        SingleOutputStreamOperator<DemandSupplyMetrics> demandMetrics = rideStream
                .filter(event -> event.getEventType().toString().equals("RIDE_REQUESTED"))
                .map(new MapFunction<RideEvent, Tuple3<String, String, Long>>() {
                    @Override
                    public Tuple3<String, String, Long> map(RideEvent event) {
                        // Extract geohash from pickup location for spatial partitioning
                        String geohash = generateGeohash(
                                event.getPickupLocation().getLatitude(),
                                event.getPickupLocation().getLongitude(),
                                6 // precision level
                        );
                        return new Tuple3<>(geohash, "DEMAND", 1L);
                    }
                })
                .keyBy(tuple -> tuple.f0) // Key by geohash
                .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
                .aggregate(new DemandSupplyCalculator.DemandAggregator())
                .name("demand-calculation");
        
        // Calculate supply metrics by location
        SingleOutputStreamOperator<DemandSupplyMetrics> supplyMetrics = driverStream
                .filter(event -> event.getStatus().toString().equals("AVAILABLE"))
                .map(new MapFunction<DriverEvent, Tuple3<String, String, Long>>() {
                    @Override
                    public Tuple3<String, String, Long> map(DriverEvent event) {
                        String geohash = generateGeohash(
                                event.getLocation().getLatitude(),
                                event.getLocation().getLongitude(),
                                6
                        );
                        return new Tuple3<>(geohash, "SUPPLY", 1L);
                    }
                })
                .keyBy(tuple -> tuple.f0)
                .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
                .aggregate(new DemandSupplyCalculator.SupplyAggregator())
                .name("supply-calculation");
        
        // Combine demand and supply metrics
        DataStream<DemandSupplyMetrics> combinedMetrics = demandMetrics
                .union(supplyMetrics)
                .keyBy(metrics -> metrics.getGeohash())
                .window(SlidingEventTimeWindows.of(Time.minutes(2), Time.seconds(30)))
                .process(new DemandSupplyCalculator())
                .name("demand-supply-combination");
        
        // Calculate surge pricing based on demand/supply ratio
        SingleOutputStreamOperator<PricingUpdate> pricingUpdates = combinedMetrics
                .process(new SurgePricingFunction())
                .name("surge-pricing-calculation");
        
        // Filter significant pricing changes (avoid noise)
        DataStream<PricingUpdate> significantPricingChanges = pricingUpdates
                .filter(update -> Math.abs(update.getSurgeMultiplier() - 1.0) > 0.1) // Only changes > 10%
                .name("filter-significant-changes");
        
        // Send pricing updates to Kafka and Pinot
        significantPricingChanges
                .addSink(new PricingSink())
                .name("pricing-updates-sink");
        
        // Monitor extreme surge conditions for alerts
        pricingUpdates
                .filter(update -> update.getSurgeMultiplier() > 3.0) // Surge > 3x
                .addSink(new PricingSink.AlertSink())
                .name("extreme-surge-alerts");
        
        // Historical pricing data for analytics
        pricingUpdates
                .addSink(new PricingSink.HistoricalSink())
                .name("historical-pricing-sink");
        
        logger.info("Dynamic Pricing Job configured successfully");
    }
    
    /**
     * Simple geohash generation for spatial partitioning
     */
    private String generateGeohash(double latitude, double longitude, int precision) {
        // Simplified geohash implementation
        // In production, use a proper geohash library
        long latBits = Double.doubleToLongBits(latitude);
        long lonBits = Double.doubleToLongBits(longitude);
        
        StringBuilder geohash = new StringBuilder();
        for (int i = 0; i < precision && i < 32; i++) {
            geohash.append(((lonBits >> (63 - i)) & 1));
            geohash.append(((latBits >> (63 - i)) & 1));
        }
        
        return geohash.toString();
    }
}