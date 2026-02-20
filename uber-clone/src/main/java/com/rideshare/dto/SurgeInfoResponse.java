package com.rideshare.dto;

import java.math.BigDecimal;

/**
 * DTO for surge pricing information response
 */
public class SurgeInfoResponse {
    
    private BigDecimal multiplier;
    private String level; // LOW, MEDIUM, HIGH
    private String message;
    private boolean isActive;
    
    // Constructors
    public SurgeInfoResponse() {}
    
    public SurgeInfoResponse(BigDecimal multiplier, String level) {
        this.multiplier = multiplier;
        this.level = level;
        this.isActive = multiplier.compareTo(BigDecimal.ONE) > 0;
        this.message = generateMessage(level, multiplier);
    }
    
    public SurgeInfoResponse(BigDecimal multiplier, String level, String message) {
        this.multiplier = multiplier;
        this.level = level;
        this.message = message;
        this.isActive = multiplier.compareTo(BigDecimal.ONE) > 0;
    }
    
    private String generateMessage(String level, BigDecimal multiplier) {
        if (!isActive) {
            return "Normal pricing is in effect";
        }
        
        return switch (level) {
            case "LOW" -> "Slightly higher demand in this area";
            case "MEDIUM" -> "Higher demand - fares are " + multiplier + "x normal rates";
            case "HIGH" -> "Very high demand - fares are " + multiplier + "x normal rates";
            default -> "Dynamic pricing is active";
        };
    }
    
    // Getters and Setters
    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
    
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}