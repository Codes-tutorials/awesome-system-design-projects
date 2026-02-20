package com.rideshare.dto;

import com.rideshare.model.Trip;
import com.rideshare.model.TripStatus;
import com.rideshare.model.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for trip response
 */
public class TripResponse {
    
    private String tripId;
    private TripStatus status;
    private VehicleType vehicleType;
    
    // Locations
    private LocationDto pickup;
    private LocationDto destination;
    
    // Trip details
    private BigDecimal estimatedDistanceKm;
    private Integer estimatedDurationMinutes;
    private BigDecimal actualDistanceKm;
    private Integer actualDurationMinutes;
    
    // Pricing
    private BigDecimal baseFare;
    private BigDecimal distanceFare;
    private BigDecimal timeFare;
    private BigDecimal surgeMultiplier;
    private BigDecimal surgeFare;
    private BigDecimal totalFare;
    
    // Driver information (when assigned)
    private DriverDto driver;
    private VehicleDto vehicle;
    
    // Timestamps
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime driverArrivedAt;
    private LocalDateTime tripStartedAt;
    private LocalDateTime tripCompletedAt;
    private LocalDateTime cancelledAt;
    
    // Additional info
    private String specialInstructions;
    private Integer estimatedArrivalMinutes;
    
    // Constructors
    public TripResponse() {}
    
    /**
     * Create TripResponse from Trip entity
     */
    public static TripResponse fromTrip(Trip trip) {
        TripResponse response = new TripResponse();
        
        response.setTripId(trip.getTripId());
        response.setStatus(trip.getStatus());
        response.setVehicleType(trip.getRequestedVehicleType());
        
        // Set locations
        response.setPickup(new LocationDto(
            trip.getPickupLatitude(),
            trip.getPickupLongitude(),
            trip.getPickupAddress(),
            trip.getPickupLandmark()
        ));
        
        response.setDestination(new LocationDto(
            trip.getDestinationLatitude(),
            trip.getDestinationLongitude(),
            trip.getDestinationAddress(),
            trip.getDestinationLandmark()
        ));
        
        // Set trip details
        response.setEstimatedDistanceKm(trip.getEstimatedDistanceKm());
        response.setEstimatedDurationMinutes(trip.getEstimatedDurationMinutes());
        response.setActualDistanceKm(trip.getActualDistanceKm());
        response.setActualDurationMinutes(trip.getActualDurationMinutes());
        
        // Set pricing
        response.setBaseFare(trip.getBaseFare());
        response.setDistanceFare(trip.getDistanceFare());
        response.setTimeFare(trip.getTimeFare());
        response.setSurgeMultiplier(trip.getSurgeMultiplier());
        response.setSurgeFare(trip.getSurgeFare());
        response.setTotalFare(trip.getTotalFare());
        
        // Set driver info if available
        if (trip.getDriver() != null) {
            response.setDriver(DriverDto.fromDriver(trip.getDriver()));
        }
        
        if (trip.getVehicle() != null) {
            response.setVehicle(VehicleDto.fromVehicle(trip.getVehicle()));
        }
        
        // Set timestamps
        response.setRequestedAt(trip.getRequestedAt());
        response.setAcceptedAt(trip.getAcceptedAt());
        response.setDriverArrivedAt(trip.getDriverArrivedAt());
        response.setTripStartedAt(trip.getTripStartedAt());
        response.setTripCompletedAt(trip.getTripCompletedAt());
        response.setCancelledAt(trip.getCancelledAt());
        
        // Set additional info
        response.setSpecialInstructions(trip.getSpecialInstructions());
        
        return response;
    }
    
    // Getters and Setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    
    public LocationDto getPickup() { return pickup; }
    public void setPickup(LocationDto pickup) { this.pickup = pickup; }
    
    public LocationDto getDestination() { return destination; }
    public void setDestination(LocationDto destination) { this.destination = destination; }
    
    public BigDecimal getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(BigDecimal estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public Integer getActualDurationMinutes() { return actualDurationMinutes; }
    public void setActualDurationMinutes(Integer actualDurationMinutes) { this.actualDurationMinutes = actualDurationMinutes; }
    
    public BigDecimal getBaseFare() { return baseFare; }
    public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
    
    public BigDecimal getDistanceFare() { return distanceFare; }
    public void setDistanceFare(BigDecimal distanceFare) { this.distanceFare = distanceFare; }
    
    public BigDecimal getTimeFare() { return timeFare; }
    public void setTimeFare(BigDecimal timeFare) { this.timeFare = timeFare; }
    
    public BigDecimal getSurgeMultiplier() { return surgeMultiplier; }
    public void setSurgeMultiplier(BigDecimal surgeMultiplier) { this.surgeMultiplier = surgeMultiplier; }
    
    public BigDecimal getSurgeFare() { return surgeFare; }
    public void setSurgeFare(BigDecimal surgeFare) { this.surgeFare = surgeFare; }
    
    public BigDecimal getTotalFare() { return totalFare; }
    public void setTotalFare(BigDecimal totalFare) { this.totalFare = totalFare; }
    
    public DriverDto getDriver() { return driver; }
    public void setDriver(DriverDto driver) { this.driver = driver; }
    
    public VehicleDto getVehicle() { return vehicle; }
    public void setVehicle(VehicleDto vehicle) { this.vehicle = vehicle; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    
    public LocalDateTime getDriverArrivedAt() { return driverArrivedAt; }
    public void setDriverArrivedAt(LocalDateTime driverArrivedAt) { this.driverArrivedAt = driverArrivedAt; }
    
    public LocalDateTime getTripStartedAt() { return tripStartedAt; }
    public void setTripStartedAt(LocalDateTime tripStartedAt) { this.tripStartedAt = tripStartedAt; }
    
    public LocalDateTime getTripCompletedAt() { return tripCompletedAt; }
    public void setTripCompletedAt(LocalDateTime tripCompletedAt) { this.tripCompletedAt = tripCompletedAt; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) { this.estimatedArrivalMinutes = estimatedArrivalMinutes; }
    
    // Nested DTOs
    public static class LocationDto {
        private Double latitude;
        private Double longitude;
        private String address;
        private String landmark;
        
        public LocationDto() {}
        
        public LocationDto(Double latitude, Double longitude, String address, String landmark) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.landmark = landmark;
        }
        
        // Getters and setters
        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }
        
        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getLandmark() { return landmark; }
        public void setLandmark(String landmark) { this.landmark = landmark; }
    }
    
    public static class DriverDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String profilePictureUrl;
        private BigDecimal rating;
        private Integer totalTrips;
        private Double currentLatitude;
        private Double currentLongitude;
        
        public static DriverDto fromDriver(com.rideshare.model.Driver driver) {
            DriverDto dto = new DriverDto();
            dto.setId(driver.getId());
            dto.setFirstName(driver.getUser().getFirstName());
            dto.setLastName(driver.getUser().getLastName());
            dto.setPhoneNumber(driver.getUser().getPhoneNumber());
            dto.setProfilePictureUrl(driver.getUser().getProfilePictureUrl());
            dto.setRating(driver.getAverageRating());
            dto.setTotalTrips(driver.getCompletedTrips());
            dto.setCurrentLatitude(driver.getCurrentLatitude());
            dto.setCurrentLongitude(driver.getCurrentLongitude());
            return dto;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
        
        public BigDecimal getRating() { return rating; }
        public void setRating(BigDecimal rating) { this.rating = rating; }
        
        public Integer getTotalTrips() { return totalTrips; }
        public void setTotalTrips(Integer totalTrips) { this.totalTrips = totalTrips; }
        
        public Double getCurrentLatitude() { return currentLatitude; }
        public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
        
        public Double getCurrentLongitude() { return currentLongitude; }
        public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    }
    
    public static class VehicleDto {
        private String licensePlate;
        private String make;
        private String model;
        private Integer year;
        private String color;
        private VehicleType vehicleType;
        
        public static VehicleDto fromVehicle(com.rideshare.model.Vehicle vehicle) {
            VehicleDto dto = new VehicleDto();
            dto.setLicensePlate(vehicle.getLicensePlate());
            dto.setMake(vehicle.getMake());
            dto.setModel(vehicle.getModel());
            dto.setYear(vehicle.getYear());
            dto.setColor(vehicle.getColor());
            dto.setVehicleType(vehicle.getVehicleType());
            return dto;
        }
        
        // Getters and setters
        public String getLicensePlate() { return licensePlate; }
        public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
        
        public String getMake() { return make; }
        public void setMake(String make) { this.make = make; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public VehicleType getVehicleType() { return vehicleType; }
        public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    }
}