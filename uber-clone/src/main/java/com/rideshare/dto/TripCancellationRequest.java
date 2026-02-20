package com.rideshare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for trip cancellation request
 */
public class TripCancellationRequest {
    
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
    
    // Constructors
    public TripCancellationRequest() {}
    
    public TripCancellationRequest(String reason) {
        this.reason = reason;
    }
    
    // Getters and Setters
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}