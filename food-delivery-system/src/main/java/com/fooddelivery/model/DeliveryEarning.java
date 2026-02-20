package com.fooddelivery.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DeliveryEarning entity to track delivery partner earnings
 */
@Entity
@Table(name = "delivery_earnings")
public class DeliveryEarning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "base_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal baseAmount;
    
    @Column(name = "distance_bonus", precision = 8, scale = 2)
    private BigDecimal distanceBonus = BigDecimal.ZERO;
    
    @Column(name = "time_bonus", precision = 8, scale = 2)
    private BigDecimal timeBonus = BigDecimal.ZERO;
    
    @Column(name = "peak_hour_bonus", precision = 8, scale = 2)
    private BigDecimal peakHourBonus = BigDecimal.ZERO;
    
    @Column(name = "tip_amount", precision = 8, scale = 2)
    private BigDecimal tipAmount = BigDecimal.ZERO;
    
    @Column(name = "total_earning", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalEarning;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;
    
    @Column(name = "commission_amount", precision = 8, scale = 2)
    private BigDecimal commissionAmount;
    
    @Column(name = "net_earning", nullable = false, precision = 8, scale = 2)
    private BigDecimal netEarning;
    
    @Column(name = "distance_km", precision = 8, scale = 2)
    private BigDecimal distanceKm;
    
    @Column(name = "delivery_time_minutes")
    private Integer deliveryTimeMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private EarningPaymentStatus paymentStatus = EarningPaymentStatus.PENDING;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    private DeliveryPartner deliveryPartner;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    // Constructors
    public DeliveryEarning() {}
    
    public DeliveryEarning(DeliveryPartner deliveryPartner, Order order, BigDecimal baseAmount) {
        this.deliveryPartner = deliveryPartner;
        this.order = order;
        this.baseAmount = baseAmount;
        this.commissionRate = deliveryPartner.getCommissionRate();
        calculateEarnings();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { 
        this.baseAmount = baseAmount;
        calculateEarnings();
    }
    
    public BigDecimal getDistanceBonus() { return distanceBonus; }
    public void setDistanceBonus(BigDecimal distanceBonus) { 
        this.distanceBonus = distanceBonus;
        calculateEarnings();
    }
    
    public BigDecimal getTimeBonus() { return timeBonus; }
    public void setTimeBonus(BigDecimal timeBonus) { 
        this.timeBonus = timeBonus;
        calculateEarnings();
    }
    
    public BigDecimal getPeakHourBonus() { return peakHourBonus; }
    public void setPeakHourBonus(BigDecimal peakHourBonus) { 
        this.peakHourBonus = peakHourBonus;
        calculateEarnings();
    }
    
    public BigDecimal getTipAmount() { return tipAmount; }
    public void setTipAmount(BigDecimal tipAmount) { 
        this.tipAmount = tipAmount;
        calculateEarnings();
    }
    
    public BigDecimal getTotalEarning() { return totalEarning; }
    public void setTotalEarning(BigDecimal totalEarning) { this.totalEarning = totalEarning; }
    
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { 
        this.commissionRate = commissionRate;
        calculateEarnings();
    }
    
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    
    public BigDecimal getNetEarning() { return netEarning; }
    public void setNetEarning(BigDecimal netEarning) { this.netEarning = netEarning; }
    
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
    
    public Integer getDeliveryTimeMinutes() { return deliveryTimeMinutes; }
    public void setDeliveryTimeMinutes(Integer deliveryTimeMinutes) { this.deliveryTimeMinutes = deliveryTimeMinutes; }
    
    public EarningPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(EarningPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public DeliveryPartner getDeliveryPartner() { return deliveryPartner; }
    public void setDeliveryPartner(DeliveryPartner deliveryPartner) { this.deliveryPartner = deliveryPartner; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    // Utility methods
    private void calculateEarnings() {
        // Calculate total earning
        totalEarning = baseAmount
            .add(distanceBonus != null ? distanceBonus : BigDecimal.ZERO)
            .add(timeBonus != null ? timeBonus : BigDecimal.ZERO)
            .add(peakHourBonus != null ? peakHourBonus : BigDecimal.ZERO)
            .add(tipAmount != null ? tipAmount : BigDecimal.ZERO);
        
        // Calculate commission
        if (commissionRate != null) {
            commissionAmount = totalEarning.multiply(commissionRate).divide(new BigDecimal("100"));
            netEarning = totalEarning.subtract(commissionAmount);
        } else {
            commissionAmount = BigDecimal.ZERO;
            netEarning = totalEarning;
        }
    }
    
    public void markAsPaid() {
        this.paymentStatus = EarningPaymentStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }
}

enum EarningPaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED
}