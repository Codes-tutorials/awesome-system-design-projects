package com.ipl.ticketbooking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "seat_number"}),
       indexes = {
           @Index(name = "idx_seat_match_category", columnList = "match_id, seat_category_id"),
           @Index(name = "idx_seat_status", columnList = "status"),
           @Index(name = "idx_seat_lock", columnList = "locked_until")
       })
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_category_id", nullable = false)
    private SeatCategory seatCategory;
    
    @Column(name = "seat_number", nullable = false)
    private String seatNumber; // A1, A2, VIP-1, etc.
    
    @Column(name = "row_number", nullable = false)
    private String rowNumber;
    
    @Column(name = "seat_in_row", nullable = false)
    private Integer seatInRow;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    // Optimistic locking for concurrent booking prevention
    @Version
    private Long version;
    
    // Temporary lock mechanism for booking process
    @Column(name = "locked_by_user")
    private Long lockedByUser;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public Seat() {}
    
    public Seat(Match match, SeatCategory seatCategory, String seatNumber, 
                String rowNumber, Integer seatInRow, BigDecimal price) {
        this.match = match;
        this.seatCategory = seatCategory;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.seatInRow = seatInRow;
        this.price = price;
    }
    
    // Business methods
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE && !isLocked();
    }
    
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public boolean isLockedByUser(Long userId) {
        return isLocked() && lockedByUser != null && lockedByUser.equals(userId);
    }
    
    public void lockForUser(Long userId, int lockDurationMinutes) {
        this.lockedByUser = userId;
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }
    
    public void releaseLock() {
        this.lockedByUser = null;
        this.lockedUntil = null;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    
    public SeatCategory getSeatCategory() { return seatCategory; }
    public void setSeatCategory(SeatCategory seatCategory) { this.seatCategory = seatCategory; }
    
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    
    public String getRowNumber() { return rowNumber; }
    public void setRowNumber(String rowNumber) { this.rowNumber = rowNumber; }
    
    public Integer getSeatInRow() { return seatInRow; }
    public void setSeatInRow(Integer seatInRow) { this.seatInRow = seatInRow; }
    
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Long getLockedByUser() { return lockedByUser; }
    public void setLockedByUser(Long lockedByUser) { this.lockedByUser = lockedByUser; }
    
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

enum SeatStatus {
    AVAILABLE, BOOKED, BLOCKED, MAINTENANCE
}