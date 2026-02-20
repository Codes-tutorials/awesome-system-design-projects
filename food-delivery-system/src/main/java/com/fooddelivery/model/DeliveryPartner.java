package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DeliveryPartner entity representing delivery personnel
 */
@Entity
@Table(name = "delivery_partners")
public class DeliveryPartner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "partner_id", unique = true, nullable = false)
    private String partnerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;
    
    @Column(name = "vehicle_number")
    private String vehicleNumber;
    
    @Column(name = "license_number")
    private String licenseNumber;
    
    @Column(name = "license_expiry_date")
    private LocalDateTime licenseExpiryDate;
    
    @Column(name = "current_latitude")
    private Double currentLatitude;
    
    @Column(name = "current_longitude")
    private Double currentLongitude;
    
    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;
    
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;
    
    @Column(name = "successful_deliveries")
    private Integer successfulDeliveries = 0;
    
    @Column(name = "cancelled_deliveries")
    private Integer cancelledDeliveries = 0;
    
    @Column(name = "average_delivery_time_minutes")
    private Integer averageDeliveryTimeMinutes;
    
    @Column(name = "total_earnings", precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    
    @Column(name = "current_month_earnings", precision = 10, scale = 2)
    private BigDecimal currentMonthEarnings = BigDecimal.ZERO;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("15.00"); // 15% default
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;
    
    @Column(name = "bank_account_holder_name")
    private String bankAccountHolderName;
    
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;
    
    @Column(name = "working_hours_start")
    private Integer workingHoursStart; // Hour of day (0-23)
    
    @Column(name = "working_hours_end")
    private Integer workingHoursEnd; // Hour of day (0-23)
    
    @Column(name = "max_delivery_radius_km")
    private Double maxDeliveryRadiusKm = 10.0;
    
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    @Column(name = "onboarding_completed_at")
    private LocalDateTime onboardingCompletedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "deliveryPartner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    @OneToMany(mappedBy = "deliveryPartner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryEarning> earnings;
    
    // Constructors
    public DeliveryPartner() {}
    
    public DeliveryPartner(User user, VehicleType vehicleType, String vehicleNumber) {
        this.user = user;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.partnerId = generatePartnerId();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public LocalDateTime getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDateTime licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
    
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    
    public LocalDateTime getLocationUpdatedAt() { return locationUpdatedAt; }
    public void setLocationUpdatedAt(LocalDateTime locationUpdatedAt) { this.locationUpdatedAt = locationUpdatedAt; }
    
    public AvailabilityStatus getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Integer getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    
    public Integer getSuccessfulDeliveries() { return successfulDeliveries; }
    public void setSuccessfulDeliveries(Integer successfulDeliveries) { this.successfulDeliveries = successfulDeliveries; }
    
    public Integer getCancelledDeliveries() { return cancelledDeliveries; }
    public void setCancelledDeliveries(Integer cancelledDeliveries) { this.cancelledDeliveries = cancelledDeliveries; }
    
    public Integer getAverageDeliveryTimeMinutes() { return averageDeliveryTimeMinutes; }
    public void setAverageDeliveryTimeMinutes(Integer averageDeliveryTimeMinutes) { this.averageDeliveryTimeMinutes = averageDeliveryTimeMinutes; }
    
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    public BigDecimal getCurrentMonthEarnings() { return currentMonthEarnings; }
    public void setCurrentMonthEarnings(BigDecimal currentMonthEarnings) { this.currentMonthEarnings = currentMonthEarnings; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    
    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }
    
    public String getBankAccountHolderName() { return bankAccountHolderName; }
    public void setBankAccountHolderName(String bankAccountHolderName) { this.bankAccountHolderName = bankAccountHolderName; }
    
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    
    public Integer getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(Integer workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    
    public Integer getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(Integer workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
    
    public Double getMaxDeliveryRadiusKm() { return maxDeliveryRadiusKm; }
    public void setMaxDeliveryRadiusKm(Double maxDeliveryRadiusKm) { this.maxDeliveryRadiusKm = maxDeliveryRadiusKm; }
    
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
    
    public LocalDateTime getOnboardingCompletedAt() { return onboardingCompletedAt; }
    public void setOnboardingCompletedAt(LocalDateTime onboardingCompletedAt) { this.onboardingCompletedAt = onboardingCompletedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    public List<DeliveryEarning> getEarnings() { return earnings; }
    public void setEarnings(List<DeliveryEarning> earnings) { this.earnings = earnings; }
    
    // Utility methods
    public boolean isAvailable() {
        return isOnline && isActive && isVerified && availabilityStatus == AvailabilityStatus.AVAILABLE;
    }
    
    public boolean isInWorkingHours() {
        if (workingHoursStart == null || workingHoursEnd == null) {
            return true; // 24/7 if no working hours set
        }
        
        int currentHour = LocalDateTime.now().getHour();
        if (workingHoursStart <= workingHoursEnd) {
            return currentHour >= workingHoursStart && currentHour < workingHoursEnd;
        } else {
            // Handles overnight shifts (e.g., 22:00 to 06:00)
            return currentHour >= workingHoursStart || currentHour < workingHoursEnd;
        }
    }
    
    public BigDecimal getSuccessRate() {
        if (totalDeliveries == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(successfulDeliveries)
                .divide(new BigDecimal(totalDeliveries), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    public void updateLocation(Double latitude, Double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.locationUpdatedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    public void updateRating(BigDecimal newRating) {
        if (totalRatings == 0) {
            averageRating = newRating;
            totalRatings = 1;
        } else {
            BigDecimal totalScore = averageRating.multiply(new BigDecimal(totalRatings));
            totalScore = totalScore.add(newRating);
            totalRatings++;
            averageRating = totalScore.divide(new BigDecimal(totalRatings), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
    
    public void addEarning(BigDecimal amount) {
        totalEarnings = totalEarnings.add(amount);
        currentMonthEarnings = currentMonthEarnings.add(amount);
    }
    
    public void completeDelivery(boolean successful) {
        totalDeliveries++;
        if (successful) {
            successfulDeliveries++;
        } else {
            cancelledDeliveries++;
        }
    }
    
    private String generatePartnerId() {
        return "DP" + System.currentTimeMillis();
    }
}

enum VehicleType {
    BICYCLE,
    MOTORCYCLE,
    SCOOTER,
    CAR,
    VAN
}

enum AvailabilityStatus {
    AVAILABLE,
    BUSY,
    OFFLINE,
    ON_BREAK
}