package com.rideshare.dto;

import com.rideshare.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for fare estimation request
 */
public class FareEstimateRequest {
    
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;
    
    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;
    
    @NotBlank(message = "Destination address is required")
    private String destinationAddress;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    // Constructors
    public FareEstimateRequest() {}
    
    public FareEstimateRequest(Double pickupLatitude, Double pickupLongitude, String pickupAddress,
                              Double destinationLatitude, Double destinationLongitude, String destinationAddress,
                              VehicleType vehicleType) {
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.pickupAddress = pickupAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationAddress = destinationAddress;
        this.vehicleType = vehicleType;
    }
    
    // Getters and Setters
    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    
    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }
    
    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }
    
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
}