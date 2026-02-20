package com.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotBlank(message = "Currency is required")
    private String currency = "USD";
    
    private String paymentMethod;
    
    @NotBlank(message = "Shipping address ID is required")
    private String shippingAddressId;
    
    private String billingAddressId;
    
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    private String notes;
    
    private String source = "WEB";
    
    private Boolean isFlashSale = false;
    
    private String flashSaleId;
    
    private String couponCode;
    
    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(String userId, List<OrderItemRequest> items, String shippingAddressId) {
        this.userId = userId;
        this.items = items;
        this.shippingAddressId = shippingAddressId;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<OrderItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
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
    
    public String getCouponCode() {
        return couponCode;
    }
    
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
    
    // Helper methods
    public BigDecimal calculateSubtotal() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal calculateTotalAmount() {
        return calculateSubtotal()
                .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .add(shippingAmount != null ? shippingAmount : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
    
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(OrderItemRequest::getQuantity)
                .sum();
    }
    
    public boolean hasPhysicalItems() {
        return items.stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.getIsDigital()));
    }
    
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "userId='" + userId + '\'' +
                ", itemCount=" + (items != null ? items.size() : 0) +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", isFlashSale=" + isFlashSale +
                ", source='" + source + '\'' +
                '}';
    }
    
    // Nested class for order items
    public static class OrderItemRequest {
        
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotBlank(message = "SKU is required")
        private String sku;
        
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        private BigDecimal unitPrice;
        
        private String productName;
        
        private String category;
        
        private String brand;
        
        private BigDecimal weight;
        
        private String dimensions;
        
        private Boolean isDigital = false;
        
        private Boolean isGiftWrap = false;
        
        private String giftMessage;
        
        private String specialInstructions;
        
        // Constructors
        public OrderItemRequest() {}
        
        public OrderItemRequest(String productId, String sku, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        // Getters and Setters
        public String getProductId() {
            return productId;
        }
        
        public void setProductId(String productId) {
            this.productId = productId;
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
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
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
        
        // Helper methods
        public BigDecimal getTotalPrice() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        
        @Override
        public String toString() {
            return "OrderItemRequest{" +
                    "productId='" + productId + '\'' +
                    ", sku='" + sku + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", isDigital=" + isDigital +
                    '}';
        }
    }
}