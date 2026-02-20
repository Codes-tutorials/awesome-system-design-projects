package com.rideshare.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for trip rating request
 */
public class TripRatingRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    private String feedback;
    
    // Constructors
    public TripRatingRequest() {}
    
    public TripRatingRequest(Integer rating, String feedback) {
        this.rating = rating;
        this.feedback = feedback;
    }
    
    // Getters and Setters
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}