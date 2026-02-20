package com.uber.analytics.models;

import java.io.Serializable;

/**
 * Model class for demand and supply metrics by location
 */
public class DemandSupplyMetrics implements Serializable {
    
    private String locationKey;
    private int demandCount;
    private int supplyCount;
    private double demandSupplyRatio;
    private String rideType;
    private long windowStart;
    private long windowEnd;
    
    // Default constructor for serialization
    public DemandSupplyMetrics() {}
    
    private DemandSupplyMetrics(Builder builder) {
        this.locationKey = builder.locationKey;
        this.demandCount = builder.demandCount;
        this.supplyCount = builder.supplyCount;
        this.demandSupplyRatio = builder.demandSupplyRatio;
        this.rideType = builder.rideType;
        this.windowStart = builder.windowStart;
        this.windowEnd = builder.windowEnd;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getLocationKey() { return locationKey; }
    public int getDemandCount() { return demandCount; }
    public int getSupplyCount() { return supplyCount; }
    public double getDemandSupplyRatio() { return demandSupplyRatio; }
    public String getRideType() { return rideType; }
    public long getWindowStart() { return windowStart; }
    public long getWindowEnd() { return windowEnd; }
    
    // Setters for serialization
    public void setLocationKey(String locationKey) { this.locationKey = locationKey; }
    public void setDemandCount(int demandCount) { this.demandCount = demandCount; }
    public void setSupplyCount(int supplyCount) { this.supplyCount = supplyCount; }
    public void setDemandSupplyRatio(double demandSupplyRatio) { this.demandSupplyRatio = demandSupplyRatio; }
    public void setRideType(String rideType) { this.rideType = rideType; }
    public void setWindowStart(long windowStart) { this.windowStart = windowStart; }
    public void setWindowEnd(long windowEnd) { this.windowEnd = windowEnd; }
    
    public static class Builder {
        private String locationKey;
        private int demandCount;
        private int supplyCount;
        private double demandSupplyRatio;
        private String rideType;
        private long windowStart;
        private long windowEnd;
        
        public Builder locationKey(String locationKey) {
            this.locationKey = locationKey;
            return this;
        }
        
        public Builder demandCount(int demandCount) {
            this.demandCount = demandCount;
            return this;
        }
        
        public Builder supplyCount(int supplyCount) {
            this.supplyCount = supplyCount;
            return this;
        }
        
        public Builder demandSupplyRatio(double demandSupplyRatio) {
            this.demandSupplyRatio = demandSupplyRatio;
            return this;
        }
        
        public Builder rideType(String rideType) {
            this.rideType = rideType;
            return this;
        }
        
        public Builder windowStart(long windowStart) {
            this.windowStart = windowStart;
            return this;
        }
        
        public Builder windowEnd(long windowEnd) {
            this.windowEnd = windowEnd;
            return this;
        }
        
        public DemandSupplyMetrics build() {
            return new DemandSupplyMetrics(this);
        }
    }
    
    @Override
    public String toString() {
        return "DemandSupplyMetrics{" +
                "locationKey='" + locationKey + '\'' +
                ", demandCount=" + demandCount +
                ", supplyCount=" + supplyCount +
                ", demandSupplyRatio=" + demandSupplyRatio +
                ", rideType='" + rideType + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                '}';
    }
}