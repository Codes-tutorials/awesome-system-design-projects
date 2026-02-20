package com.rideshare.dto;

import com.rideshare.model.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO for trip request
 */
public class TripRequestDto {
    
    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid pickup latitude")
    @DecimalMax(value = "90.0", message = "Invalid pickup latitude")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid pickup longitude")
    @DecimalMax(value = "180.0", message = "Invalid pickup longitude")
    private Double pickupLongitude;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    private String pickupLandmark;
    
    @NotNull(message = "Destination latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid destination latitude")
    @DecimalMax(value = "90.0", message = "Invalid destination latitude")
    private Double destinationLatitude;
    
    @NotNull(message = "Destination longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid destination longitude")
    @DecimalMax(value = "180.0", message = "Invalid destination longitude")
    private Double destinationLongitude;
    
    @NotBlank(message = "Destination address is required")
    private String destinationAddress;
    
    private String destinationLandmark;
    
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;
    
    private String specialInstructions;
    
    private String riderNotes;
    
    // Constructors
    public TripRequestDto() {}
    
    public TripRequestDto(Double pickupLatitude, Double pickupLongitude, String pickupAddress,
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
    
    public String getPickupLandmark() { return pickupLandmark; }
    public void setPickupLandmark(String pickupLandmark) { this.pickupLandmark = pickupLandmark; }
    
    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }
    
    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }
    
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    
    public String getDestinationLandmark() { return destinationLandmark; }
    public void setDestinationLandmark(String destinationLandmark) { this.destinationLandmark = destinationLandmark; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getRiderNotes() { return riderNotes; }
    public void setRiderNotes(String riderNotes) { this.riderNotes = riderNotes; }
}