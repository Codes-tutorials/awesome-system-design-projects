package com.rideshare.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vehicle entity for driver's vehicle information
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;
    
    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Invalid license plate format")
    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;
    
    @NotBlank(message = "Make is required")
    @Column(nullable = false)
    private String make;
    
    @NotBlank(message = "Model is required")
    @Column(nullable = false)
    private String model;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private String color;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;
    
    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;
    
    @Column(name = "vin_number", unique = true)
    private String vinNumber;
    
    @Column(name = "registration_number", unique = true)
    private String registrationNumber;
    
    @Column(name = "registration_expiry_date")
    private LocalDate registrationExpiryDate;
    
    @Column(name = "insurance_policy_number")
    private String insurancePolicyNumber;
    
    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;
    
    @Column(name = "inspection_expiry_date")
    private LocalDate inspectionExpiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VehicleVerificationStatus verificationStatus = VehicleVerificationStatus.PENDING;
    
    @Column(name = "vehicle_image_url")
    private String vehicleImageUrl;
    
    @Column(name = "registration_document_url")
    private String registrationDocumentUrl;
    
    @Column(name = "insurance_document_url")
    private String insuranceDocumentUrl;
    
    @Column(name = "inspection_certificate_url")
    private String inspectionCertificateUrl;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Vehicle() {}
    
    public Vehicle(Driver driver, String licensePlate, String make, String model, 
                   Integer year, String color, VehicleType vehicleType, Integer seatingCapacity) {
        this.driver = driver;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.vehicleType = vehicleType;
        this.seatingCapacity = seatingCapacity;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    
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
    
    public Integer getSeatingCapacity() { return seatingCapacity; }
    public void setSeatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; }
    
    public String getVinNumber() { return vinNumber; }
    public void setVinNumber(String vinNumber) { this.vinNumber = vinNumber; }
    
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    
    public LocalDate getRegistrationExpiryDate() { return registrationExpiryDate; }
    public void setRegistrationExpiryDate(LocalDate registrationExpiryDate) { this.registrationExpiryDate = registrationExpiryDate; }
    
    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }
    
    public LocalDate getInsuranceExpiryDate() { return insuranceExpiryDate; }
    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; }
    
    public LocalDate getInspectionExpiryDate() { return inspectionExpiryDate; }
    public void setInspectionExpiryDate(LocalDate inspectionExpiryDate) { this.inspectionExpiryDate = inspectionExpiryDate; }
    
    public VehicleVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VehicleVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    
    public String getVehicleImageUrl() { return vehicleImageUrl; }
    public void setVehicleImageUrl(String vehicleImageUrl) { this.vehicleImageUrl = vehicleImageUrl; }
    
    public String getRegistrationDocumentUrl() { return registrationDocumentUrl; }
    public void setRegistrationDocumentUrl(String registrationDocumentUrl) { this.registrationDocumentUrl = registrationDocumentUrl; }
    
    public String getInsuranceDocumentUrl() { return insuranceDocumentUrl; }
    public void setInsuranceDocumentUrl(String insuranceDocumentUrl) { this.insuranceDocumentUrl = insuranceDocumentUrl; }
    
    public String getInspectionCertificateUrl() { return inspectionCertificateUrl; }
    public void setInspectionCertificateUrl(String inspectionCertificateUrl) { this.inspectionCertificateUrl = inspectionCertificateUrl; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public String getDisplayName() {
        return year + " " + make + " " + model;
    }
    
    public boolean isVerified() {
        return verificationStatus == VehicleVerificationStatus.APPROVED;
    }
    
    public boolean isDocumentationComplete() {
        return registrationNumber != null && 
               insurancePolicyNumber != null &&
               registrationExpiryDate != null &&
               insuranceExpiryDate != null &&
               inspectionExpiryDate != null;
    }
    
    public boolean isDocumentationValid() {
        LocalDate now = LocalDate.now();
        return registrationExpiryDate != null && registrationExpiryDate.isAfter(now) &&
               insuranceExpiryDate != null && insuranceExpiryDate.isAfter(now) &&
               inspectionExpiryDate != null && inspectionExpiryDate.isAfter(now);
    }
}

enum VehicleType {
    ECONOMY("Economy", 4),
    COMFORT("Comfort", 4),
    PREMIUM("Premium", 4),
    SUV("SUV", 6),
    LUXURY("Luxury", 4),
    VAN("Van", 8);
    
    private final String displayName;
    private final int defaultCapacity;
    
    VehicleType(String displayName, int defaultCapacity) {
        this.displayName = displayName;
        this.defaultCapacity = defaultCapacity;
    }
    
    public String getDisplayName() { return displayName; }
    public int getDefaultCapacity() { return defaultCapacity; }
}

enum VehicleVerificationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED,
    SUSPENDED
}