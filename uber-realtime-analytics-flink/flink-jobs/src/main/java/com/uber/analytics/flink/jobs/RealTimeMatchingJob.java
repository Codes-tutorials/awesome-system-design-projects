package com.uber.analytics.flink.jobs;

import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.RideEvent;
import com.uber.analytics.flink.functions.DriverMatchingFunction;
import com.uber.analytics.flink.functions.RideRequestProcessFunction;
import com.uber.analytics.flink.sinks.MatchingSink;
import com.uber.analytics.flink.sources.DriverEventSource;
import com.uber.analytics.flink.sources.RideEventSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Real-time driver-rider matching job
 * Processes ride requests and available drivers to find optimal matches
 */
public class RealTimeMatchingJob {
    
    private static final Logger logger = LoggerFactory.getLogger(RealTimeMatchingJob.class);
    
    public void execute(StreamExecutionEnvironment env) {
        logger.info("Configuring Real-Time Matching Job");
        
        // Configure ride request stream
        DataStream<RideEvent> rideRequestStream = env
                .addSource(new RideEventSource())
                .name("ride-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<RideEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                )
                .filter(new FilterFunction<RideEvent>() {
                    @Override
                    public boolean filter(RideEvent event) {
                        return event.getEventType().toString().equals("RIDE_REQUESTED");
                    }
                })
                .name("filter-ride-requests");
        
        // Configure driver location stream
        DataStream<DriverEvent> driverLocationStream = env
                .addSource(new DriverEventSource())
                .name("driver-events-source")
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<DriverEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
                )
                .filter(new FilterFunction<DriverEvent>() {
                    @Override
                    public boolean filter(DriverEvent event) {
                        return event.getStatus().toString().equals("AVAILABLE") || 
                               event.getEventType().toString().equals("LOCATION_UPDATE");
                    }
                })
                .name("filter-available-drivers");
        
        // Process ride requests with context
        SingleOutputStreamOperator<RideEvent> processedRideRequests = rideRequestStream
                .process(new RideRequestProcessFunction())
                .name("process-ride-requests");
        
        // Create driver availability windows
        DataStream<DriverEvent> driverAvailabilityWindows = driverLocationStream
                .keyBy(event -> event.getDriverId().toString())
                .window(TumblingEventTimeWindows.of(Time.seconds(30)))
                .reduce((driver1, driver2) -> {
                    // Keep the latest driver state
                    return driver1.getTimestamp() > driver2.getTimestamp() ? driver1 : driver2;
                })
                .name("driver-availability-windows");
        
        // Connect ride requests with driver availability
        DataStream<Tuple2<RideEvent, DriverEvent>> matchingCandidates = processedRideRequests
                .connect(driverAvailabilityWindows)
                .process(new DriverMatchingFunction())
                .name("driver-matching");
        
        // Filter successful matches and send to output
        matchingCandidates
                .filter(match -> match.f0 != null && match.f1 != null)
                .addSink(new MatchingSink())
                .name("matching-results-sink");
        
        // Send unmatched requests for retry or alternative handling
        matchingCandidates
                .filter(match -> match.f0 != null && match.f1 == null)
                .map(unmatchedRequest -> unmatchedRequest.f0)
                .addSink(new MatchingSink.UnmatchedRequestsSink())
                .name("unmatched-requests-sink");
        
        logger.info("Real-Time Matching Job configured successfully");
    }
}