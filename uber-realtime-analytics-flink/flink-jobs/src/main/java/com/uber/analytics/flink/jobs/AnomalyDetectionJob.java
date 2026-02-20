package com.uber.analytics.flink.jobs;

import com.uber.analytics.events.RideEvent;
import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.OrderEvent;
import com.uber.analytics.flink.functions.AnomalyDetector;
import com.uber.analytics.flink.functions.FraudDetector;
import com.uber.analytics.flink.functions.PatternDetector;
import com.uber.analytics.flink.model.AnomalyAlert;
import com.uber.analytics.flink.sinks.AnomalySink;
import com.uber.analytics.flink.sources.RideEventSource;
import com.uber.analytics.flink.sources.DriverEventSource;
import com.uber.analytics.flink.sources.OrderEventSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Anomaly detection job that identifies unusual patterns and potential fraud
 */
public class AnomalyDetectionJob {
    
    private static final Logger logger = LoggerFactory.getLogger(AnomalyDetectionJob.class);
    
    public void execute(StreamExecutionEnvironment env) {
        logger.info("Configuring Anomaly Detection Job");
        
        // Configure event streams
        DataStream<RideEvent> rideStream = env
                .addSource(new RideEventSource())
                .name("ride-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<RideEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
        
        DataStream<DriverEvent> driverStream = env
                .addSource(new DriverEventSource())
                .name("driver-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<DriverEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
        
        DataStream<OrderEvent> orderStream = env
                .addSource(new OrderEventSource())
                .name("order-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<OrderEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                );
        
        // === RIDE ANOMALY DETECTION ===
        
        // Detect unusual ride patterns (e.g., very short or very long rides)
        SingleOutputStreamOperator<AnomalyAlert> rideAnomalies = rideStream
                .filter(event -> event.getEventType().toString().equals("RIDE_COMPLETED"))
                .filter(new FilterFunction<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        // Filter rides with distance and duration data
                        return event.getDistance() != null && event.getDuration() != null;
                    }
                })
                .keyBy(event -> extractCity(event.getPickupLocation().getLatitude(), 
                                          event.getPickupLocation().getLongitude()))
                .window(SlidingEventTimeWindows.of(Time.minutes(10), Time.minutes(2)))
                .process(new AnomalyDetector.RideAnomalyDetector())
                .name("ride-anomaly-detection");
        
        // Detect surge pricing anomalies
        SingleOutputStreamOperator<AnomalyAlert> pricingAnomalies = rideStream
                .filter(event -> event.getSurgeMultiplier() != null && event.getSurgeMultiplier() > 1.0)
                .map(new MapFunction<RideEvent, Tuple3<String, Double, Long>>() {
                    @Override
                    public Tuple3<String, Double, Long> map(RideEvent event) {
                        String city = extractCity(event.getPickupLocation().getLatitude(), 
                                                event.getPickupLocation().getLongitude());
                        return new Tuple3<>(city, event.getSurgeMultiplier(), event.getTimestamp());
                    }
                })
                .keyBy(tuple -> tuple.f0)
                .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
                .process(new AnomalyDetector.PricingAnomalyDetector())
                .name("pricing-anomaly-detection");
        
        // === DRIVER BEHAVIOR ANOMALY DETECTION ===
        
        // Detect unusual driver behavior patterns
        SingleOutputStreamOperator<AnomalyAlert> driverAnomalies = driverStream
                .keyBy(event -> event.getDriverId().toString())
                .window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(10)))
                .process(new AnomalyDetector.DriverBehaviorDetector())
                .name("driver-behavior-anomaly-detection");
        
        // === FRAUD DETECTION USING CEP ===
        
        // Pattern: Multiple ride cancellations by the same rider in short time
        Pattern<RideEvent, ?> suspiciousCancellationPattern = Pattern
                .<RideEvent>begin("first_cancellation")
                .where(new SimpleCondition<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("RIDE_CANCELLED");
                    }
                })
                .next("second_cancellation")
                .where(new SimpleCondition<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("RIDE_CANCELLED");
                    }
                })
                .next("third_cancellation")
                .where(new SimpleCondition<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("RIDE_CANCELLED");
                    }
                })
                .within(Time.minutes(30));
        
        PatternStream<RideEvent> suspiciousCancellations = CEP.pattern(
                rideStream.keyBy(event -> event.getRiderId().toString()),
                suspiciousCancellationPattern
        );
        
        SingleOutputStreamOperator<AnomalyAlert> fraudAlerts = suspiciousCancellations
                .process(new FraudDetector.SuspiciousCancellationDetector())
                .name("fraud-detection-cancellations");
        
        // Pattern: Driver accepting and immediately cancelling rides
        Pattern<RideEvent, ?> driverFraudPattern = Pattern
                .<RideEvent>begin("accepted")
                .where(new SimpleCondition<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("DRIVER_ASSIGNED");
                    }
                })
                .next("cancelled")
                .where(new SimpleCondition<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("RIDE_CANCELLED");
                    }
                })
                .within(Time.minutes(2));
        
        PatternStream<RideEvent> driverFraudPatterns = CEP.pattern(
                rideStream.keyBy(event -> event.getDriverId() != null ? event.getDriverId().toString() : "unknown"),
                driverFraudPattern
        );
        
        SingleOutputStreamOperator<AnomalyAlert> driverFraudAlerts = driverFraudPatterns
                .process(new FraudDetector.DriverFraudDetector())
                .name("fraud-detection-drivers");
        
        // === ORDER ANOMALY DETECTION ===
        
        // Detect unusual order patterns (e.g., very high value orders, unusual locations)
        SingleOutputStreamOperator<AnomalyAlert> orderAnomalies = orderStream
                .filter(event -> event.getEventType().toString().equals("ORDER_PLACED"))
                .keyBy(event -> event.getCustomerId().toString())
                .window(SlidingEventTimeWindows.of(Time.hours(2), Time.minutes(15)))
                .process(new AnomalyDetector.OrderAnomalyDetector())
                .name("order-anomaly-detection");
        
        // === SYSTEM HEALTH ANOMALY DETECTION ===
        
        // Monitor system-wide metrics for anomalies
        DataStream<Tuple3<String, String, Long>> systemMetrics = rideStream
                .map(event -> new Tuple3<>("SYSTEM", "RIDE_EVENT", 1L))
                .union(driverStream.map(event -> new Tuple3<>("SYSTEM", "DRIVER_EVENT", 1L)))
                .union(orderStream.map(event -> new Tuple3<>("SYSTEM", "ORDER_EVENT", 1L)));
        
        SingleOutputStreamOperator<AnomalyAlert> systemAnomalies = systemMetrics
                .keyBy(tuple -> tuple.f1)
                .window(SlidingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
                .process(new AnomalyDetector.SystemHealthDetector())
                .name("system-health-anomaly-detection");
        
        // === COMBINE ALL ANOMALIES ===
        
        DataStream<AnomalyAlert> allAnomalies = rideAnomalies
                .union(pricingAnomalies)
                .union(driverAnomalies)
                .union(fraudAlerts)
                .union(driverFraudAlerts)
                .union(orderAnomalies)
                .union(systemAnomalies);
        
        // === OUTPUT SINKS ===
        
        // Send high-severity anomalies to immediate alerting system
        allAnomalies
                .filter(alert -> alert.getSeverity().equals("HIGH") || alert.getSeverity().equals("CRITICAL"))
                .addSink(new AnomalySink.ImmediateAlertSink())
                .name("immediate-alerts-sink");
        
        // Send all anomalies to monitoring dashboard
        allAnomalies
                .addSink(new AnomalySink.DashboardSink())
                .name("anomaly-dashboard-sink");
        
        // Send fraud alerts to security team
        DataStream<AnomalyAlert> fraudAlertsOnly = allAnomalies
                .filter(alert -> alert.getType().contains("FRAUD"));
        
        fraudAlertsOnly
                .addSink(new AnomalySink.SecurityAlertSink())
                .name("security-alerts-sink");
        
        // Store all anomalies for historical analysis
        allAnomalies
                .addSink(new AnomalySink.HistoricalSink())
                .name("anomaly-historical-sink");
        
        logger.info("Anomaly Detection Job configured successfully");
    }
    
    /**
     * Extract city from coordinates (simplified implementation)
     */
    private String extractCity(double latitude, double longitude) {
        if (latitude >= 37.7 && latitude <= 37.8 && longitude >= -122.5 && longitude <= -122.4) {
            return "San Francisco";
        } else if (latitude >= 40.7 && latitude <= 40.8 && longitude >= -74.1 && longitude <= -73.9) {
            return "New York";
        } else if (latitude >= 34.0 && latitude <= 34.1 && longitude >= -118.3 && longitude <= -118.2) {
            return "Los Angeles";
        }
        return "Other";
    }
}