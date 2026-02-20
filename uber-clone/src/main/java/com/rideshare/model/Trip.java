package com.rideshare.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trip entity representing a ride from pickup to destination
 */
@Entity
@Table(name = "trips")
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "trip_id", unique = true, nullable = false)
    private String tripId; // Public facing trip ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trip_status", nullable = false)
    private TripStatus status = TripStatus.REQUESTED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType requestedVehicleType;
    
    // Pickup Location
    @Column(name = "pickup_latitude", nullable = false)
    private Double pickupLatitude;
    
    @Column(name = "pickup_longitude", nullable = false)
    private Double pickupLongitude;
    
    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;
    
    @Column(name = "pickup_landmark")
    private String pickupLandmark;
    
    // Destination Location
    @Column(name = "destination_latitude", nullable = false)
    private Double destinationLatitude;
    
    @Column(name = "destination_longitude", nullable = false)
    private Double destinationLongitude;
    
    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;
    
    @Column(name = "destination_landmark")
    private String destinationLandmark;
    
    // Trip Details
    @Column(name = "estimated_distance_km", precision = 8, scale = 2)
    private BigDecimal estimatedDistanceKm;
    
    @Column(name = "actual_distance_km", precision = 8, scale = 2)
    private BigDecimal actualDistanceKm;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @Column(name = "actual_duration_minutes")
    private Integer actualDurationMinutes;
    
    // Pricing
    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @Column(name = "distance_fare", precision = 10, scale = 2)
    private BigDecimal distanceFare;
    
    @Column(name = "time_fare", precision = 10, scale = 2)
    private BigDecimal timeFare;
    
    @Column(name = "surge_multiplier", precision = 3, scale = 2)
    private BigDecimal surgeMultiplier = BigDecimal.ONE;
    
    @Column(name = "surge_fare", precision = 10, scale = 2)
    private BigDecimal surgeFare = BigDecimal.ZERO;
    
    @Column(name = "total_fare", precision = 10, scale = 2)
    private BigDecimal totalFare;
    
    @Column(name = "driver_earnings", precision = 10, scale = 2)
    private BigDecimal driverEarnings;
    
    @Column(name = "platform_commission", precision = 10, scale = 2)
    private BigDecimal platformCommission;
    
    // Timestamps
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "driver_arrived_at")
    private LocalDateTime driverArrivedAt;
    
    @Column(name = "trip_started_at")
    private LocalDateTime tripStartedAt;
    
    @Column(name = "trip_completed_at")
    private LocalDateTime tripCompletedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // Cancellation
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;
    
    // Payment
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;
    
    // Ratings and Feedback
    @Column(name = "rider_rating")
    private Integer riderRating; // 1-5 stars
    
    @Column(name = "driver_rating")
    private Integer driverRating; // 1-5 stars
    
    @Column(name = "rider_feedback")
    private String riderFeedback;
    
    @Column(name = "driver_feedback")
    private String driverFeedback;
    
    // Special Instructions
    @Column(name = "special_instructions")
    private String specialInstructions;
    
    @Column(name = "rider_notes")
    private String riderNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Trip() {
        this.requestedAt = LocalDateTime.now();
    }
    
    public Trip(User rider, VehicleType requestedVehicleType, 
                Double pickupLatitude, Double pickupLongitude, String pickupAddress,
                Double destinationLatitude, Double destinationLongitude, String destinationAddress) {
        this();
        this.rider = rider;
        this.requestedVehicleType = requestedVehicleType;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.pickupAddress = pickupAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationAddress = destinationAddress;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    
    public User getRider() { return rider; }
    public void setRider(User rider) { this.rider = rider; }
    
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
    
    public VehicleType getRequestedVehicleType() { return requestedVehicleType; }
    public void setRequestedVehicleType(VehicleType requestedVehicleType) { this.requestedVehicleType = requestedVehicleType; }
    
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
    
    public BigDecimal getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(BigDecimal estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }
    
    public BigDecimal getActualDistanceKm() { return actualDistanceKm; }
    public void setActualDistanceKm(BigDecimal actualDistanceKm) { this.actualDistanceKm = actualDistanceKm; }
    
    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    
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
    
    public BigDecimal getDriverEarnings() { return driverEarnings; }
    public void setDriverEarnings(BigDecimal driverEarnings) { this.driverEarnings = driverEarnings; }
    
    public BigDecimal getPlatformCommission() { return platformCommission; }
    public void setPlatformCommission(BigDecimal platformCommission) { this.platformCommission = platformCommission; }
    
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
    
    public CancelledBy getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(CancelledBy cancelledBy) { this.cancelledBy = cancelledBy; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }
    
    public Integer getRiderRating() { return riderRating; }
    public void setRiderRating(Integer riderRating) { this.riderRating = riderRating; }
    
    public Integer getDriverRating() { return driverRating; }
    public void setDriverRating(Integer driverRating) { this.driverRating = driverRating; }
    
    public String getRiderFeedback() { return riderFeedback; }
    public void setRiderFeedback(String riderFeedback) { this.riderFeedback = riderFeedback; }
    
    public String getDriverFeedback() { return driverFeedback; }
    public void setDriverFeedback(String driverFeedback) { this.driverFeedback = driverFeedback; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getRiderNotes() { return riderNotes; }
    public void setRiderNotes(String riderNotes) { this.riderNotes = riderNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public boolean isActive() {
        return status == TripStatus.REQUESTED || 
               status == TripStatus.ACCEPTED || 
               status == TripStatus.DRIVER_ARRIVED || 
               status == TripStatus.IN_PROGRESS;
    }
    
    public boolean isCompleted() {
        return status == TripStatus.COMPLETED;
    }
    
    public boolean isCancelled() {
        return status == TripStatus.CANCELLED;
    }
    
    public boolean canBeCancelled() {
        return status == TripStatus.REQUESTED || 
               status == TripStatus.ACCEPTED || 
               status == TripStatus.DRIVER_ARRIVED;
    }
    
    public void acceptTrip(Driver driver) {
        this.driver = driver;
        this.vehicle = driver.getUser().getId() != null ? 
            // Assuming we have a way to get vehicle by driver
            null : null; // This would be set by the service layer
        this.status = TripStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
    
    public void startTrip() {
        this.status = TripStatus.IN_PROGRESS;
        this.tripStartedAt = LocalDateTime.now();
    }
    
    public void completeTrip() {
        this.status = TripStatus.COMPLETED;
        this.tripCompletedAt = LocalDateTime.now();
    }
    
    public void cancelTrip(CancelledBy cancelledBy, String reason) {
        this.status = TripStatus.CANCELLED;
        this.cancelledBy = cancelledBy;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
}

enum TripStatus {
    REQUESTED,
    ACCEPTED,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum CancelledBy {
    RIDER,
    DRIVER,
    SYSTEM
}

enum PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    DIGITAL_WALLET,
    UPI
}

enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}