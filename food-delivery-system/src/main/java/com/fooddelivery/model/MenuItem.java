package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MenuItem entity representing food items in restaurant menus
 */
@Entity
@Table(name = "menu_items")
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private MenuCategory category;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Column(name = "is_vegetarian")
    private Boolean isVegetarian = false;
    
    @Column(name = "is_vegan")
    private Boolean isVegan = false;
    
    @Column(name = "is_gluten_free")
    private Boolean isGlutenFree = false;
    
    @Column(name = "is_spicy")
    private Boolean isSpicy = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level")
    private SpiceLevel spiceLevel;
    
    @Column(name = "calories")
    private Integer calories;
    
    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;
    
    @Column(name = "ingredients")
    private String ingredients;
    
    @Column(name = "allergens")
    private String allergens;
    
    @Column(name = "nutritional_info")
    private String nutritionalInfo;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    @Column(name = "total_ratings")
    private Integer totalRatings = 0;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "discounted_price", precision = 8, scale = 2)
    private BigDecimal discountedPrice;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartItem> cartItems;
    
    // Constructors
    public MenuItem() {}
    
    public MenuItem(String name, String description, BigDecimal price, MenuCategory category, Restaurant restaurant) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.restaurant = restaurant;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }
    
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    
    public Boolean getIsVegetarian() { return isVegetarian; }
    public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }
    
    public Boolean getIsVegan() { return isVegan; }
    public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }
    
    public Boolean getIsGlutenFree() { return isGlutenFree; }
    public void setIsGlutenFree(Boolean isGlutenFree) { this.isGlutenFree = isGlutenFree; }
    
    public Boolean getIsSpicy() { return isSpicy; }
    public void setIsSpicy(Boolean isSpicy) { this.isSpicy = isSpicy; }
    
    public SpiceLevel getSpiceLevel() { return spiceLevel; }
    public void setSpiceLevel(SpiceLevel spiceLevel) { this.spiceLevel = spiceLevel; }
    
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    
    public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
    public void setPreparationTimeMinutes(Integer preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
    
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    
    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }
    
    public String getNutritionalInfo() { return nutritionalInfo; }
    public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    
    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public List<CartItem> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItem> cartItems) { this.cartItems = cartItems; }
    
    // Utility methods
    public BigDecimal getEffectivePrice() {
        if (discountedPrice != null && discountedPrice.compareTo(BigDecimal.ZERO) > 0) {
            return discountedPrice;
        }
        return price;
    }
    
    public boolean hasDiscount() {
        return discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public void applyDiscount(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
        BigDecimal discountAmount = price.multiply(discountPercentage).divide(new BigDecimal("100"));
        this.discountedPrice = price.subtract(discountAmount);
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
    
    public void incrementOrderCount() {
        totalOrders = (totalOrders == null) ? 1 : totalOrders + 1;
    }
}

enum MenuCategory {
    APPETIZERS,
    MAIN_COURSE,
    DESSERTS,
    BEVERAGES,
    SOUPS,
    SALADS,
    SIDES,
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACKS,
    COMBO_MEALS,
    SPECIALS,
    SEASONAL,
    KIDS_MENU,
    HEALTHY_OPTIONS,
    VEGAN_OPTIONS,
    GLUTEN_FREE_OPTIONS
}

enum SpiceLevel {
    MILD,
    MEDIUM,
    HOT,
    EXTRA_HOT
}