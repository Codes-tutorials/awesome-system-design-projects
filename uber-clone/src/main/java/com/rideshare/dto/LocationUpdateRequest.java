package com.rideshare.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for location update request
 */
public class LocationUpdateRequest {
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    private Double heading; // Direction in degrees
    private Double speed; // Speed in km/h
    
    // Constructors
    public LocationUpdateRequest() {}
    
    public LocationUpdateRequest(Double latitude, Double longitude, Double heading, Double speed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.heading = heading;
        this.speed = speed;
    }
    
    // Getters and Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }
    
    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }
}