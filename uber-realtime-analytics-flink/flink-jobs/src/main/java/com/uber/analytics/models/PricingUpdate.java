package com.uber.analytics.models;

import java.io.Serializable;

/**
 * Model class for pricing updates
 */
public class PricingUpdate implements Serializable {
    
    private String locationKey;
    private double surgeMultiplier;
    private double baseFare;
    private long timestamp;
    private int demandCount;
    private int supplyCount;
    private double demandSupplyRatio;
    
    // Default constructor for serialization
    public PricingUpdate() {}
    
    private PricingUpdate(Builder builder) {
        this.locationKey = builder.locationKey;
        this.surgeMultiplier = builder.surgeMultiplier;
        this.baseFare = builder.baseFare;
        this.timestamp = builder.timestamp;
        this.demandCount = builder.demandCount;
        this.supplyCount = builder.supplyCount;
        this.demandSupplyRatio = builder.demandSupplyRatio;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getLocationKey() { return locationKey; }
    public double getSurgeMultiplier() { return surgeMultiplier; }
    public double getBaseFare() { return baseFare; }
    public long getTimestamp() { return timestamp; }
    public int getDemandCount() { return demandCount; }
    public int getSupplyCount() { return supplyCount; }
    public double getDemandSupplyRatio() { return demandSupplyRatio; }
    
    // Setters for serialization
    public void setLocationKey(String locationKey) { this.locationKey = locationKey; }
    public void setSurgeMultiplier(double surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setDemandCount(int demandCount) { this.demandCount = demandCount; }
    public void setSupplyCount(int supplyCount) { this.supplyCount = supplyCount; }
    public void setDemandSupplyRatio(double demandSupplyRatio) { this.demandSupplyRatio = demandSupplyRatio; }
    
    public static class Builder {
        private String locationKey;
        private double surgeMultiplier;
        private double baseFare;
        private long timestamp;
        private int demandCount;
        private int supplyCount;
        private double demandSupplyRatio;
        
        public Builder locationKey(String locationKey) {
            this.locationKey = locationKey;
            return this;
        }
        
        public Builder surgeMultiplier(double surgeMultiplier) {
            this.surgeMultiplier = surgeMultiplier;
            return this;
        }
        
        public Builder baseFare(double baseFare) {
            this.baseFare = baseFare;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
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
        
        public PricingUpdate build() {
            return new PricingUpdate(this);
        }
    }
    
    @Override
    public String toString() {
        return "PricingUpdate{" +
                "locationKey='" + locationKey + '\'' +
                ", surgeMultiplier=" + surgeMultiplier +
                ", baseFare=" + baseFare +
                ", timestamp=" + timestamp +
                ", demandCount=" + demandCount +
                ", supplyCount=" + supplyCount +
                ", demandSupplyRatio=" + demandSupplyRatio +
                '}';
    }
}