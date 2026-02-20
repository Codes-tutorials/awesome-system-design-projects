package com.rideshare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for trip rejection request (Driver)
 */
public class TripRejectionRequest {
    
    @NotBlank(message = "Rejection reason is required")
    private String reason;
    
    // Constructors
    public TripRejectionRequest() {}
    
    public TripRejectionRequest(String reason) {
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}