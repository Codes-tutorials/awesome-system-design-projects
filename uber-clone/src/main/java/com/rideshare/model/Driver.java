package com.rideshare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Driver entity with additional driver-specific information
 */
@Entity
@Table(name = "drivers")
public class Driver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @NotBlank(message = "License number is required")
    @Column(name = "license_number", unique = true, nullable = false)
    private String licenseNumber;
    
    @Column(name = "license_expiry_date", nullable = false)
    private LocalDate licenseExpiryDate;
    
    @Column(name = "license_image_url")
    private String licenseImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @Column(name = "current_heading")
    private Double currentHeading; // Direction in degrees
    
    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;
    
    @Column(name = "earnings_today", precision = 10, scale = 2)
    private BigDecimal earningsToday = BigDecimal.ZERO;
    
    @Column(name = "earnings_this_week", precision = 10, scale = 2)
    private BigDecimal earningsThisWeek = BigDecimal.ZERO;
    
    @Column(name = "earnings_this_month", precision = 10, scale = 2)
    private BigDecimal earningsThisMonth = BigDecimal.ZERO;
    
    @Column(name = "total_earnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    
    @Column(name = "completed_trips")
    private Integer completedTrips = 0;
    
    @Column(name = "cancelled_trips")
    private Integer cancelledTrips = 0;
    
    @Column(name = "acceptance_rate", precision = 5, scale = 2)
    private BigDecimal acceptanceRate = BigDecimal.ZERO;
    
    @Column(name = "cancellation_rate", precision = 5, scale = 2)
    private BigDecimal cancellationRate = BigDecimal.ZERO;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;
    
    @Column(name = "last_trip_completed_at")
    private LocalDateTime lastTripCompletedAt;
    
    @Column(name = "background_check_status")
    @Enumerated(EnumType.STRING)
    private BackgroundCheckStatus backgroundCheckStatus = BackgroundCheckStatus.PENDING;
    
    @Column(name = "background_check_date")
    private LocalDate backgroundCheckDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Driver() {}
    
    public Driver(User user, String licenseNumber, LocalDate licenseExpiryDate) {
        this.user = user;
        this.licenseNumber = licenseNumber;
        this.licenseExpiryDate = licenseExpiryDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
    
    public String getLicenseImageUrl() { return licenseImageUrl; }
    public void setLicenseImageUrl(String licenseImageUrl) { this.licenseImageUrl = licenseImageUrl; }
    
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public Double getCurrentHeading() { return currentHeading; }
    public void setCurrentHeading(Double currentHeading) { this.currentHeading = currentHeading; }
    
    public LocalDateTime getLocationUpdatedAt() { return locationUpdatedAt; }
    public void setLocationUpdatedAt(LocalDateTime locationUpdatedAt) { this.locationUpdatedAt = locationUpdatedAt; }
    
    public BigDecimal getEarningsToday() { return earningsToday; }
    public void setEarningsToday(BigDecimal earningsToday) { this.earningsToday = earningsToday; }
    
    public BigDecimal getEarningsThisWeek() { return earningsThisWeek; }
    public void setEarningsThisWeek(BigDecimal earningsThisWeek) { this.earningsThisWeek = earningsThisWeek; }
    
    public BigDecimal getEarningsThisMonth() { return earningsThisMonth; }
    public void setEarningsThisMonth(BigDecimal earningsThisMonth) { this.earningsThisMonth = earningsThisMonth; }
    
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    public Integer getCompletedTrips() { return completedTrips; }
    public void setCompletedTrips(Integer completedTrips) { this.completedTrips = completedTrips; }
    
    public Integer getCancelledTrips() { return cancelledTrips; }
    public void setCancelledTrips(Integer cancelledTrips) { this.cancelledTrips = cancelledTrips; }
    
    public BigDecimal getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(BigDecimal acceptanceRate) { this.acceptanceRate = acceptanceRate; }
    
    public BigDecimal getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(BigDecimal cancellationRate) { this.cancellationRate = cancellationRate; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    
    public LocalDateTime getLastTripCompletedAt() { return lastTripCompletedAt; }
    public void setLastTripCompletedAt(LocalDateTime lastTripCompletedAt) { this.lastTripCompletedAt = lastTripCompletedAt; }
    
    public BackgroundCheckStatus getBackgroundCheckStatus() { return backgroundCheckStatus; }
    public void setBackgroundCheckStatus(BackgroundCheckStatus backgroundCheckStatus) { this.backgroundCheckStatus = backgroundCheckStatus; }
    
    public LocalDate getBackgroundCheckDate() { return backgroundCheckDate; }
    public void setBackgroundCheckDate(LocalDate backgroundCheckDate) { this.backgroundCheckDate = backgroundCheckDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public boolean isAvailable() {
        return verificationStatus == VerificationStatus.APPROVED &&
               availabilityStatus == AvailabilityStatus.AVAILABLE &&
               isOnline &&
               backgroundCheckStatus == BackgroundCheckStatus.APPROVED;
    }
    
    public boolean isVerified() {
        return verificationStatus == VerificationStatus.APPROVED &&
               backgroundCheckStatus == BackgroundCheckStatus.APPROVED;
    }
    
    public void updateLocation(Double latitude, Double longitude, Double heading) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.currentHeading = heading;
        this.locationUpdatedAt = LocalDateTime.now();
    }
    
    public void addEarnings(BigDecimal amount) {
        this.earningsToday = this.earningsToday.add(amount);
        this.earningsThisWeek = this.earningsThisWeek.add(amount);
        this.earningsThisMonth = this.earningsThisMonth.add(amount);
        this.totalEarnings = this.totalEarnings.add(amount);
    }
    
    public void incrementCompletedTrips() {
        this.completedTrips++;
        this.lastTripCompletedAt = LocalDateTime.now();
    }
    
    public void incrementCancelledTrips() {
        this.cancelledTrips++;
    }
}

enum VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

enum AvailabilityStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}

enum BackgroundCheckStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}