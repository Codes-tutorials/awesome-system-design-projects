package com.uber.analytics.flink.jobs;

import com.uber.analytics.events.RideEvent;
import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.OrderEvent;
import com.uber.analytics.flink.functions.MetricsAggregator;
import com.uber.analytics.flink.functions.KPICalculator;
import com.uber.analytics.flink.model.BusinessMetrics;
import com.uber.analytics.flink.model.OperationalMetrics;
import com.uber.analytics.flink.sinks.AnalyticsSink;
import com.uber.analytics.flink.sources.RideEventSource;
import com.uber.analytics.flink.sources.DriverEventSource;
import com.uber.analytics.flink.sources.OrderEventSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Analytics aggregation job that computes real-time KPIs and business metrics
 */
public class AnalyticsAggregationJob {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsAggregationJob.class);
    
    public void execute(StreamExecutionEnvironment env) {
        logger.info("Configuring Analytics Aggregation Job");
        
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
        
        // === RIDE ANALYTICS ===
        
        // Ride completion metrics
        SingleOutputStreamOperator<BusinessMetrics> rideMetrics = rideStream
                .filter(event -> event.getEventType().toString().equals("RIDE_COMPLETED"))
                .map(new MapFunction<RideEvent, Tuple4<String, String, Double, Long>>() {
                    @Override
                    public Tuple4<String, String, Double, Long> map(RideEvent event) {
                        String city = extractCity(event.getPickupLocation().getLatitude(), 
                                                event.getPickupLocation().getLongitude());
                        return new Tuple4<>(
                                city,
                                "RIDE_COMPLETED",
                                event.getActualFare() != null ? event.getActualFare() : 0.0,
                                1L
                        );
                    }
                })
                .keyBy(tuple -> tuple.f0 + "_" + tuple.f1) // Key by city and metric type
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new MetricsAggregator.RideMetricsAggregator())
                .name("ride-completion-metrics");
        
        // Ride request metrics
        SingleOutputStreamOperator<BusinessMetrics> rideRequestMetrics = rideStream
                .filter(event -> event.getEventType().toString().equals("RIDE_REQUESTED"))
                .map(new MapFunction<RideEvent, Tuple4<String, String, Double, Long>>() {
                    @Override
                    public Tuple4<String, String, Double, Long> map(RideEvent event) {
                        String city = extractCity(event.getPickupLocation().getLatitude(), 
                                                event.getPickupLocation().getLongitude());
                        return new Tuple4<>(
                                city,
                                "RIDE_REQUESTED",
                                event.getEstimatedFare() != null ? event.getEstimatedFare() : 0.0,
                                1L
                        );
                    }
                })
                .keyBy(tuple -> tuple.f0 + "_" + tuple.f1)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new MetricsAggregator.RideMetricsAggregator())
                .name("ride-request-metrics");
        
        // === DRIVER ANALYTICS ===
        
        // Driver utilization metrics
        SingleOutputStreamOperator<OperationalMetrics> driverUtilizationMetrics = driverStream
                .keyBy(event -> event.getDriverId().toString())
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .aggregate(new MetricsAggregator.DriverUtilizationAggregator())
                .name("driver-utilization-metrics");
        
        // === ORDER ANALYTICS (UberEats) ===
        
        // Order completion metrics
        SingleOutputStreamOperator<BusinessMetrics> orderMetrics = orderStream
                .filter(event -> event.getEventType().toString().equals("ORDER_DELIVERED"))
                .map(new MapFunction<OrderEvent, Tuple4<String, String, Double, Long>>() {
                    @Override
                    public Tuple4<String, String, Double, Long> map(OrderEvent event) {
                        String city = extractCity(event.getDeliveryAddress().getLatitude(), 
                                                event.getDeliveryAddress().getLongitude());
                        return new Tuple4<>(
                                city,
                                "ORDER_DELIVERED",
                                event.getTotalAmount(),
                                1L
                        );
                    }
                })
                .keyBy(tuple -> tuple.f0 + "_" + tuple.f1)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new MetricsAggregator.OrderMetricsAggregator())
                .name("order-completion-metrics");
        
        // === KPI CALCULATIONS ===
        
        // Combine all business metrics
        DataStream<BusinessMetrics> allBusinessMetrics = rideMetrics
                .union(rideRequestMetrics)
                .union(orderMetrics);
        
        // Calculate key business KPIs
        SingleOutputStreamOperator<BusinessMetrics> businessKPIs = allBusinessMetrics
                .keyBy(metrics -> metrics.getCity())
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .process(new KPICalculator.BusinessKPICalculator())
                .name("business-kpi-calculation");
        
        // Calculate operational KPIs
        SingleOutputStreamOperator<OperationalMetrics> operationalKPIs = driverUtilizationMetrics
                .keyBy(metrics -> metrics.getCity())
                .window(TumblingEventTimeWindows.of(Time.minutes(5)))
                .process(new KPICalculator.OperationalKPICalculator())
                .name("operational-kpi-calculation");
        
        // === OUTPUT SINKS ===
        
        // Send business metrics to Pinot for real-time dashboards
        businessKPIs
                .addSink(new AnalyticsSink.BusinessMetricsSink())
                .name("business-metrics-sink");
        
        // Send operational metrics to Pinot
        operationalKPIs
                .addSink(new AnalyticsSink.OperationalMetricsSink())
                .name("operational-metrics-sink");
        
        // Send detailed metrics to data lake for historical analysis
        allBusinessMetrics
                .addSink(new AnalyticsSink.DataLakeSink())
                .name("data-lake-sink");
        
        // Real-time alerts for critical KPIs
        businessKPIs
                .filter(metrics -> metrics.getConversionRate() < 0.7) // Alert if conversion < 70%
                .addSink(new AnalyticsSink.AlertSink())
                .name("low-conversion-alerts");
        
        operationalKPIs
                .filter(metrics -> metrics.getDriverUtilization() < 0.5) // Alert if utilization < 50%
                .addSink(new AnalyticsSink.AlertSink())
                .name("low-utilization-alerts");
        
        logger.info("Analytics Aggregation Job configured successfully");
    }
    
    /**
     * Extract city from coordinates (simplified implementation)
     */
    private String extractCity(double latitude, double longitude) {
        // Simplified city extraction based on coordinate ranges
        if (latitude >= 37.7 && latitude <= 37.8 && longitude >= -122.5 && longitude <= -122.4) {
            return "San Francisco";
        } else if (latitude >= 40.7 && latitude <= 40.8 && longitude >= -74.1 && longitude <= -73.9) {
            return "New York";
        } else if (latitude >= 34.0 && latitude <= 34.1 && longitude >= -118.3 && longitude <= -118.2) {
            return "Los Angeles";
        } else if (latitude >= 41.8 && latitude <= 41.9 && longitude >= -87.7 && longitude <= -87.6) {
            return "Chicago";
        }
        return "Other";
    }
}