package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Restaurant entity representing food establishments
 */
@Entity
@Table(name = "restaurants")
public class Restaurant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", nullable = false)
    private CuisineType cuisineType;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "delivery_fee", precision = 8, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column(name = "minimum_order_amount", precision = 8, scale = 2)
    private BigDecimal minimumOrderAmount;
    
    @Column(name = "estimated_delivery_time_minutes")
    private Integer estimatedDeliveryTimeMinutes;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_accepting_orders", nullable = false)
    private Boolean isAcceptingOrders = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "opening_time")
    private LocalTime openingTime;
    
    @Column(name = "closing_time")
    private LocalTime closingTime;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private Double latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private Double longitude;
    
    @Column(name = "address", nullable = false)
    private String address;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "state", nullable = false)
    private String state;
    
    @Column(name = "postal_code", nullable = false)
    private String postalCode;
    
    @Column(name = "country", nullable = false)
    private String country;
    
    @Column(name = "license_number")
    private String licenseNumber;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    // Constructors
    public Restaurant() {}
    
    public Restaurant(String name, String phoneNumber, CuisineType cuisineType, 
                     String address, String city, String state, String postalCode, String country) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.cuisineType = cuisineType;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    
    public CuisineType getCuisineType() { return cuisineType; }
    public void setCuisineType(CuisineType cuisineType) { this.cuisineType = cuisineType; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) { this.minimumOrderAmount = minimumOrderAmount; }
    
    public Integer getEstimatedDeliveryTimeMinutes() { return estimatedDeliveryTimeMinutes; }
    public void setEstimatedDeliveryTimeMinutes(Integer estimatedDeliveryTimeMinutes) { this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsAcceptingOrders() { return isAcceptingOrders; }
    public void setIsAcceptingOrders(Boolean isAcceptingOrders) { this.isAcceptingOrders = isAcceptingOrders; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public LocalTime getOpeningTime() { return openingTime; }
    public void setOpeningTime(LocalTime openingTime) { this.openingTime = openingTime; }
    
    public LocalTime getClosingTime() { return closingTime; }
    public void setClosingTime(LocalTime closingTime) { this.closingTime = closingTime; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public List<MenuItem> getMenuItems() { return menuItems; }
    public void setMenuItems(List<MenuItem> menuItems) { this.menuItems = menuItems; }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    
    // Utility methods
    public boolean isOpen() {
        if (openingTime == null || closingTime == null) {
            return true; // 24/7 if no times set
        }
        
        LocalTime now = LocalTime.now();
        if (openingTime.isBefore(closingTime)) {
            return now.isAfter(openingTime) && now.isBefore(closingTime);
        } else {
            // Handles overnight restaurants (e.g., 22:00 to 06:00)
            return now.isAfter(openingTime) || now.isBefore(closingTime);
        }
    }
    
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", address, city, state, postalCode, country);
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
}

enum CuisineType {
    INDIAN,
    CHINESE,
    ITALIAN,
    MEXICAN,
    AMERICAN,
    THAI,
    JAPANESE,
    MEDITERRANEAN,
    FAST_FOOD,
    PIZZA,
    BURGER,
    DESSERTS,
    BEVERAGES,
    HEALTHY,
    VEGAN,
    VEGETARIAN,
    SEAFOOD,
    BBQ,
    CONTINENTAL,
    SOUTH_INDIAN,
    NORTH_INDIAN,
    STREET_FOOD,
    BAKERY,
    CAFE,
    OTHER
}