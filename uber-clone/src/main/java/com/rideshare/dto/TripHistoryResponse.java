package com.rideshare.dto;

import com.rideshare.model.TripStatus;
import com.rideshare.model.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for trip history response
 */
public class TripHistoryResponse {
    
    private String tripId;
    private TripStatus status;
    private VehicleType vehicleType;
    private String pickupAddress;
    private String destinationAddress;
    private BigDecimal totalFare;
    private BigDecimal actualDistanceKm;
    private Integer actualDurationMinutes;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private Integer rating; // Rating given by user
    private String driverName;
    private String vehicleInfo; // e.g., "Honda Civic - ABC123"
    
    // Constructors
    public TripHistoryResponse() {}
    
    // Getters and Setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    
    public BigDecimal getTotalFare() { return totalFare; }
    public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }
    
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
}