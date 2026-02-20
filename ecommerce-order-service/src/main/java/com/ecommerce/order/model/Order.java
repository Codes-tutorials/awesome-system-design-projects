package com.ecommerce.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_user_id", columnList = "user_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at"),
    @Index(name = "idx_order_number", columnList = "order_number"),
    @Index(name = "idx_order_shard", columnList = "shard_key")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", unique = true, nullable = false)
    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;
    
    @Column(name = "order_number", unique = true, nullable = false)
    @NotBlank(message = "Order number cannot be blank")
    private String orderNumber;
    
    @Column(name = "user_id", nullable = false)
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Column(name = "shipping_address_id")
    private String shippingAddressId;
    
    @Column(name = "billing_address_id")
    private String billingAddressId;
    
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_amount", precision = 19, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "source")
    private String source = "WEB"; // WEB, MOBILE, API
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private OrderPriority priority = OrderPriority.NORMAL;
    
    @Column(name = "is_flash_sale")
    private Boolean isFlashSale = false;
    
    @Column(name = "flash_sale_id")
    private String flashSaleId;
    
    @Column(name = "shard_key", nullable = false)
    private Integer shardKey;
    
    @Column(name = "version")
    @Version
    private Long version = 0L;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<OrderItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    
    // Constructors
    public Order() {
        this.orderId = UUID.randomUUID().toString();
        this.orderNumber = generateOrderNumber();
    }
    
    public Order(String userId, BigDecimal totalAmount) {
        this();
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.shardKey = calculateShardKey(userId);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
        this.shardKey = calculateShardKey(userId);
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
        updateStatusTimestamp(status);
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public String getShippingAddressId() {
        return shippingAddressId;
    }
    
    public void setShippingAddressId(String shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }
    
    public String getBillingAddressId() {
        return billingAddressId;
    }
    
    public void setBillingAddressId(String billingAddressId) {
        this.billingAddressId = billingAddressId;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }
    
    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public OrderPriority getPriority() {
        return priority;
    }
    
    public void setPriority(OrderPriority priority) {
        this.priority = priority;
    }
    
    public Boolean getIsFlashSale() {
        return isFlashSale;
    }
    
    public void setIsFlashSale(Boolean isFlashSale) {
        this.isFlashSale = isFlashSale;
    }
    
    public String getFlashSaleId() {
        return flashSaleId;
    }
    
    public void setFlashSaleId(String flashSaleId) {
        this.flashSaleId = flashSaleId;
    }
    
    public Integer getShardKey() {
        return shardKey;
    }
    
    public void setShardKey(Integer shardKey) {
        this.shardKey = shardKey;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }
    
    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
    
    public LocalDateTime getShippedAt() {
        return shippedAt;
    }
    
    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    // Helper methods
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
    
    public BigDecimal calculateSubtotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal calculateFinalAmount() {
        return calculateSubtotal()
                .add(taxAmount)
                .add(shippingAmount)
                .subtract(discountAmount);
    }
    
    public boolean isModifiable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean isCancellable() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.CONFIRMED || 
               status == OrderStatus.PROCESSING;
    }
    
    public void markAsConfirmed() {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }
    
    public void markAsShipped() {
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }
    
    public void markAsDelivered() {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void markAsCancelled(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }
    
    private void updateStatusTimestamp(OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> this.confirmedAt = now;
            case SHIPPED -> this.shippedAt = now;
            case DELIVERED -> this.deliveredAt = now;
            case CANCELLED -> this.cancelledAt = now;
        }
    }
    
    private String generateOrderNumber() {
        // Generate order number with timestamp and random component
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("ORD-%d-%03d", timestamp, random);
    }
    
    private Integer calculateShardKey(String userId) {
        // Simple hash-based sharding
        return Math.abs(userId.hashCode()) % 16; // 16 shards
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                ", isFlashSale=" + isFlashSale +
                ", createdAt=" + createdAt +
                '}';
    }
}