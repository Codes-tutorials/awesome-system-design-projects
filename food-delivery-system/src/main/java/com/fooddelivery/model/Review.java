package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Review entity for restaurant and delivery reviews
 */
@Entity
@Table(name = "reviews")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars
    
    @Column(name = "comment")
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;
    
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    @Column(name = "is_helpful_count")
    private Integer isHelpfulCount = 0;
    
    @Column(name = "is_reported", nullable = false)
    private Boolean isReported = false;
    
    @Column(name = "response_from_restaurant")
    private String responseFromRestaurant;
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;
    
    // Constructors
    public Review() {}
    
    public Review(User user, Integer rating, String comment, ReviewType reviewType) {
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.reviewType = reviewType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public ReviewType getReviewType() { return reviewType; }
    public void setReviewType(ReviewType reviewType) { this.reviewType = reviewType; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Integer getIsHelpfulCount() { return isHelpfulCount; }
    public void setIsHelpfulCount(Integer isHelpfulCount) { this.isHelpfulCount = isHelpfulCount; }
    
    public Boolean getIsReported() { return isReported; }
    public void setIsReported(Boolean isReported) { this.isReported = isReported; }
    
    public String getResponseFromRestaurant() { return responseFromRestaurant; }
    public void setResponseFromRestaurant(String responseFromRestaurant) { this.responseFromRestaurant = responseFromRestaurant; }
    
    public LocalDateTime getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDateTime responseDate) { this.responseDate = responseDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public DeliveryPartner getDeliveryPartner() { return deliveryPartner; }
    public void setDeliveryPartner(DeliveryPartner deliveryPartner) { this.deliveryPartner = deliveryPartner; }
    
    // Utility methods
    public void addRestaurantResponse(String response) {
        this.responseFromRestaurant = response;
        this.responseDate = LocalDateTime.now();
    }
    
    public void incrementHelpfulCount() {
        this.isHelpfulCount = (isHelpfulCount == null) ? 1 : isHelpfulCount + 1;
    }
}

enum ReviewType {
    RESTAURANT,
    DELIVERY,
    FOOD_QUALITY,
    SERVICE
}