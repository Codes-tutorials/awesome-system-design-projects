package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory entity to track order status changes
 */
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "updated_by")
    private String updatedBy; // User ID or system
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    // Constructors
    public OrderStatusHistory() {}
    
    public OrderStatusHistory(Order order, OrderStatus status, String updatedBy) {
        this.order = order;
        this.status = status;
        this.updatedBy = updatedBy;
    }
    
    public OrderStatusHistory(Order order, OrderStatus status, String notes, String updatedBy) {
        this.order = order;
        this.status = status;
        this.notes = notes;
        this.updatedBy = updatedBy;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}