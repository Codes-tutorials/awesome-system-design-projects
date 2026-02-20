package com.rideshare.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for trip completion request (Driver)
 */
public class TripCompletionRequest {
    
    @NotNull(message = "Final latitude is required")
    private Double finalLatitude;
    
    @NotNull(message = "Final longitude is required")
    private Double finalLongitude;
    
    @NotNull(message = "Actual distance is required")
    private BigDecimal actualDistanceKm;
    
    private String notes;
    
    // Constructors
    public TripCompletionRequest() {}
    
    public TripCompletionRequest(Double finalLatitude, Double finalLongitude, 
                                BigDecimal actualDistanceKm, String notes) {
        this.finalLatitude = finalLatitude;
        this.finalLongitude = finalLongitude;
        this.actualDistanceKm = actualDistanceKm;
        this.notes = notes;
    }
    
    // Getters and Setters
    public Double getFinalLatitude() { return finalLatitude; }
    public void setFinalLatitude(Double finalLatitude) { this.finalLatitude = finalLatitude; }
    
    public Double getFinalLongitude() { return finalLongitude; }
    public void setFinalLongitude(Double finalLongitude) { this.finalLongitude = finalLongitude; }
    
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}