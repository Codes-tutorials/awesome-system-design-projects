package com.rideshare.dto;

import com.rideshare.model.VehicleType;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for fare estimation response
 */
public class FareEstimateResponse {
    
    private Double distanceKm;
    private Integer estimatedDurationMinutes;
    private List<FareOption> fareOptions;
    private SurgeInfo surgeInfo;
    
    // Constructors
    public FareEstimateResponse() {}
    
    public FareEstimateResponse(Double distanceKm, Integer estimatedDurationMinutes, 
                               List<FareOption> fareOptions, SurgeInfo surgeInfo) {
        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.fareOptions = fareOptions;
        this.surgeInfo = surgeInfo;
    }
    
    // Getters and Setters
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
    public List<FareOption> getFareOptions() { return fareOptions; }
    public void setFareOptions(List<FareOption> fareOptions) { this.fareOptions = fareOptions; }
    
    public SurgeInfo getSurgeInfo() { return surgeInfo; }
    public void setSurgeInfo(SurgeInfo surgeInfo) { this.surgeInfo = surgeInfo; }
    
    // Nested classes
    public static class FareOption {
        private VehicleType vehicleType;
        private String displayName;
        private BigDecimal baseFare;
        private BigDecimal distanceFare;
        private BigDecimal timeFare;
        private BigDecimal surgeFare;
        private BigDecimal totalFare;
        private BigDecimal surgeMultiplier;
        private Integer estimatedArrivalMinutes;
        private String description;
        
        // Constructors
        public FareOption() {}
        
        public FareOption(VehicleType vehicleType, String displayName, BigDecimal baseFare,
                         BigDecimal distanceFare, BigDecimal timeFare, BigDecimal surgeFare,
                         BigDecimal totalFare, BigDecimal surgeMultiplier, Integer estimatedArrivalMinutes,
                         String description) {
            this.vehicleType = vehicleType;
            this.displayName = displayName;
            this.baseFare = baseFare;
            this.distanceFare = distanceFare;
            this.timeFare = timeFare;
            this.surgeFare = surgeFare;
            this.totalFare = totalFare;
            this.surgeMultiplier = surgeMultiplier;
            this.estimatedArrivalMinutes = estimatedArrivalMinutes;
            this.description = description;
        }
        
        // Getters and Setters
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public BigDecimal getBaseFare() { return baseFare; }
        public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
        
        public BigDecimal getDistanceFare() { return distanceFare; }
        public void setDistanceFare(BigDecimal distanceFare) { this.distanceFare = distanceFare; }
        
        public BigDecimal getTimeFare() { return timeFare; }
        public void setTimeFare(BigDecimal timeFare) { this.timeFare = timeFare; }
        
        public BigDecimal getSurgeFare() { return surgeFare; }
        public void setSurgeFare(BigDecimal surgeFare) { this.surgeFare = surgeFare; }
        
        public BigDecimal getTotalFare() { return totalFare; }
        public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }
        
        public BigDecimal getSurgeMultiplier() { return surgeMultiplier; }
        public void setSurgeMultiplier(BigDecimal surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
        
        public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
        public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) { this.estimatedArrivalMinutes = estimatedArrivalMinutes; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    public static class SurgeInfo {
        private BigDecimal multiplier;
        private String level; // LOW, MEDIUM, HIGH
        private String message;
        
        // Constructors
        public SurgeInfo() {}
        
        public SurgeInfo(BigDecimal multiplier, String level, String message) {
            this.multiplier = multiplier;
            this.level = level;
            this.message = message;
        }
        
        // Getters and Setters
        public BigDecimal getMultiplier() { return multiplier; }
        public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}