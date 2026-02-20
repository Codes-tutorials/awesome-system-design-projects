package com.ipl.ticketbooking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seat_categories")
public class SeatCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;
    
    @Column(nullable = false)
    private String categoryName; // VIP, Premium, General, Corporate Box
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal priceMultiplier; // 1.0 for base, 2.5 for premium, etc.
    
    @Column(nullable = false)
    private Integer totalSeats;
    
    @Column(nullable = false)
    private String sectionCode; // A, B, C, VIP1, etc.
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "seatCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public SeatCategory() {}
    
    public SeatCategory(Stadium stadium, String categoryName, String description, 
                       BigDecimal priceMultiplier, Integer totalSeats, String sectionCode) {
        this.stadium = stadium;
        this.categoryName = categoryName;
        this.description = description;
        this.priceMultiplier = priceMultiplier;
        this.totalSeats = totalSeats;
        this.sectionCode = sectionCode;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Stadium getStadium() { return stadium; }
    public void setStadium(Stadium stadium) { this.stadium = stadium; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPriceMultiplier() { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }
    
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Seat> getSeats() { return seats; }
    public void setSeats(List<Seat> seats) { this.seats = seats; }
}