package com.uber.analytics.flink.functions;

import com.uber.analytics.events.DriverEvent;
import com.uber.analytics.events.RideEvent;
import com.uber.analytics.models.DemandSupplyMetrics;
import com.uber.analytics.models.PricingUpdate;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Surge pricing function that calculates dynamic pricing based on supply and demand
 */
public class SurgePricingFunction extends CoProcessFunction<DemandSupplyMetrics, Tuple2<String, Integer>, PricingUpdate> {
    
    private static final Logger logger = LoggerFactory.getLogger(SurgePricingFunction.class);
    
    // Pricing configuration
    private static final double BASE_SURGE_MULTIPLIER = 1.0;
    private static final double MAX_SURGE_MULTIPLIER = 5.0;
    private static final double MIN_SURGE_MULTIPLIER = 1.0;
    private static final double SURGE_THRESHOLD_RATIO = 1.5; // Demand/Supply ratio to trigger surge
    
    // State to store current pricing by location
    private transient MapState<String, PricingUpdate> currentPricing;
    
    // State to store demand/supply metrics by location
    private transient MapState<String, DemandSupplyMetrics> locationMetrics;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        
        // Initialize pricing state
        MapStateDescriptor<String, PricingUpdate> pricingDescriptor = 
            new MapStateDescriptor<>("current-pricing", String.class, PricingUpdate.class);
        currentPricing = getRuntimeContext().getMapState(pricingDescriptor);
        
        // Initialize metrics state
        MapStateDescriptor<String, DemandSupplyMetrics> metricsDescriptor = 
            new MapStateDescriptor<>("location-metrics", String.class, DemandSupplyMetrics.class);
        locationMetrics = getRuntimeContext().getMapState(metricsDescriptor);
        
        logger.info("SurgePricingFunction initialized");
    }
    
    @Override
    public void processElement1(DemandSupplyMetrics metrics, Context context, 
                               Collector<PricingUpdate> collector) throws Exception {
        
        String locationKey = metrics.getLocationKey();
        logger.debug("Processing demand/supply metrics for location: {}", locationKey);
        
        // Store the metrics
        locationMetrics.put(locationKey, metrics);
        
        // Calculate new surge multiplier
        double surgeMultiplier = calculateSurgeMultiplier(metrics);
        
        // Get current pricing for this location
        PricingUpdate currentPrice = currentPricing.get(locationKey);
        double currentMultiplier = currentPrice != null ? currentPrice.getSurgeMultiplier() : BASE_SURGE_MULTIPLIER;
        
        // Only update if there's a significant change (avoid noise)
        if (Math.abs(surgeMultiplier - currentMultiplier) > 0.1) {
            
            PricingUpdate newPricing = PricingUpdate.builder()
                .locationKey(locationKey)
                .surgeMultiplier(surgeMultiplier)
                .baseFare(calculateBaseFare(metrics.getRideType()))
                .timestamp(context.timestamp())
                .demandCount(metrics.getDemandCount())
                .supplyCount(metrics.getSupplyCount())
                .demandSupplyRatio(metrics.getDemandSupplyRatio())
                .build();
            
            // Update state
            currentPricing.put(locationKey, newPricing);
            
            // Emit pricing update
            collector.collect(newPricing);
            
            logger.info("Surge pricing updated for {}: {} -> {} (demand: {}, supply: {})", 
                       locationKey, currentMultiplier, surgeMultiplier, 
                       metrics.getDemandCount(), metrics.getSupplyCount());
        }
    }
    
    @Override
    public void processElement2(Tuple2<String, Integer> locationSupply, Context context, 
                               Collector<PricingUpdate> collector) throws Exception {
        
        String locationKey = locationSupply.f0;
        int supplyCount = locationSupply.f1;
        
        logger.debug("Processing supply update for location: {} - supply: {}", locationKey, supplyCount);
        
        // Get current metrics for this location
        DemandSupplyMetrics currentMetrics = locationMetrics.get(locationKey);
        
        if (currentMetrics != null) {
            // Update supply count and recalculate ratio
            DemandSupplyMetrics updatedMetrics = DemandSupplyMetrics.builder()
                .locationKey(locationKey)
                .demandCount(currentMetrics.getDemandCount())
                .supplyCount(supplyCount)
                .demandSupplyRatio(supplyCount > 0 ? (double) currentMetrics.getDemandCount() / supplyCount : Double.MAX_VALUE)
                .rideType(currentMetrics.getRideType())
                .windowStart(currentMetrics.getWindowStart())
                .windowEnd(context.timestamp())
                .build();
            
            // Process the updated metrics
            processElement1(updatedMetrics, context, collector);
        }
    }
    
    private double calculateSurgeMultiplier(DemandSupplyMetrics metrics) {
        double demandSupplyRatio = metrics.getDemandSupplyRatio();
        
        if (demandSupplyRatio <= 1.0) {
            // Supply exceeds or equals demand - no surge
            return MIN_SURGE_MULTIPLIER;
        }
        
        if (demandSupplyRatio < SURGE_THRESHOLD_RATIO) {
            // Slight demand increase - minimal surge
            return BASE_SURGE_MULTIPLIER + (demandSupplyRatio - 1.0) * 0.2;
        }
        
        // High demand - calculate surge based on ratio
        double surgeMultiplier = BASE_SURGE_MULTIPLIER + Math.log(demandSupplyRatio) * 0.8;
        
        // Apply surge multiplier based on time of day and location factors
        surgeMultiplier = applySurgeModifiers(surgeMultiplier, metrics);
        
        // Cap the surge multiplier
        return Math.min(MAX_SURGE_MULTIPLIER, Math.max(MIN_SURGE_MULTIPLIER, surgeMultiplier));
    }
    
    private double applySurgeModifiers(double baseSurge, DemandSupplyMetrics metrics) {
        double modifiedSurge = baseSurge;
        
        // Time-based modifiers
        long currentHour = (System.currentTimeMillis() / (1000 * 60 * 60)) % 24;
        
        // Peak hours (7-9 AM, 5-7 PM) - increase surge
        if ((currentHour >= 7 && currentHour <= 9) || (currentHour >= 17 && currentHour <= 19)) {
            modifiedSurge *= 1.2;
        }
        
        // Late night hours (11 PM - 5 AM) - increase surge
        if (currentHour >= 23 || currentHour <= 5) {
            modifiedSurge *= 1.3;
        }
        
        // Weekend modifier (simplified - in reality would check actual day)
        // This is a placeholder - in production, you'd have proper date/time handling
        
        // Location-based modifiers (airport, downtown, etc.)
        // This would be based on actual location data
        if (metrics.getLocationKey().contains("airport")) {
            modifiedSurge *= 1.1;
        }
        
        if (metrics.getLocationKey().contains("downtown")) {
            modifiedSurge *= 1.05;
        }
        
        return modifiedSurge;
    }
    
    private double calculateBaseFare(String rideType) {
        // Base fare calculation based on ride type
        Map<String, Double> baseFares = new HashMap<>();
        baseFares.put("UBER_X", 2.50);
        baseFares.put("UBER_XL", 3.50);
        baseFares.put("UBER_BLACK", 5.00);
        baseFares.put("UBER_POOL", 1.50);
        baseFares.put("UBER_EATS", 1.99);
        
        return baseFares.getOrDefault(rideType, 2.50);
    }
}