package com.ecommerce.order.dto;

import com.ecommerce.order.model.OrderPriority;
import com.ecommerce.order.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    
    private Long id;
    private String orderId;
    private String orderNumber;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentMethod;
    private String paymentId;
    private String shippingAddressId;
    private String billingAddressId;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private String notes;
    private String source;
    private OrderPriority priority;
    private Boolean isFlashSale;
    private String flashSaleId;
    private List<OrderItemResponse> items;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime confirmedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime shippedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;
    
    private String cancellationReason;
    
    // Constructors
    public OrderResponse() {}
    
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
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
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
    
    public List<OrderItemResponse> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemResponse> items) {
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
    public BigDecimal getSubtotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(OrderItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getTotalItemCount() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(OrderItemResponse::getQuantity)
                .sum();
    }
    
    public boolean hasPhysicalItems() {
        if (items == null) return false;
        return items.stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.getIsDigital()));
    }
    
    public String getFormattedAmount() {
        return String.format("%.2f %s", totalAmount, currency);
    }
    
    @Override
    public String toString() {
        return "OrderResponse{" +
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
    
    // Nested class for order item response
    public static class OrderItemResponse {
        
        private Long id;
        private String productId;
        private String productName;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private String category;
        private String brand;
        private BigDecimal weight;
        private String dimensions;
        private Boolean isDigital;
        private Boolean isGiftWrap;
        private String giftMessage;
        private String specialInstructions;
        
        // Constructors
        public OrderItemResponse() {}
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public String getSku() {
            return sku;
        }
        
        public void setSku(String sku) {
            this.sku = sku;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public BigDecimal getTotalPrice() {
            return totalPrice;
        }
        
        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
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
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getBrand() {
            return brand;
        }
        
        public void setBrand(String brand) {
            this.brand = brand;
        }
        
        public BigDecimal getWeight() {
            return weight;
        }
        
        public void setWeight(BigDecimal weight) {
            this.weight = weight;
        }
        
        public String getDimensions() {
            return dimensions;
        }
        
        public void setDimensions(String dimensions) {
            this.dimensions = dimensions;
        }
        
        public Boolean getIsDigital() {
            return isDigital;
        }
        
        public void setIsDigital(Boolean isDigital) {
            this.isDigital = isDigital;
        }
        
        public Boolean getIsGiftWrap() {
            return isGiftWrap;
        }
        
        public void setIsGiftWrap(Boolean isGiftWrap) {
            this.isGiftWrap = isGiftWrap;
        }
        
        public String getGiftMessage() {
            return giftMessage;
        }
        
        public void setGiftMessage(String giftMessage) {
            this.giftMessage = giftMessage;
        }
        
        public String getSpecialInstructions() {
            return specialInstructions;
        }
        
        public void setSpecialInstructions(String specialInstructions) {
            this.specialInstructions = specialInstructions;
        }
        
        public BigDecimal getFinalPrice() {
            return totalPrice.add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                           .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
        
        @Override
        public String toString() {
            return "OrderItemResponse{" +
                    "id=" + id +
                    ", productId='" + productId + '\'' +
                    ", sku='" + sku + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", totalPrice=" + totalPrice +
                    '}';
        }
    }
}