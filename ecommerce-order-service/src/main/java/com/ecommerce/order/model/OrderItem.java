package com.ecommerce.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id"),
    @Index(name = "idx_order_item_sku", columnList = "sku")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull(message = "Order cannot be null")
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;
    
    @Column(name = "product_name", nullable = false)
    @NotBlank(message = "Product name cannot be blank")
    private String productName;
    
    @Column(name = "sku", nullable = false)
    @NotBlank(message = "SKU cannot be blank")
    private String sku;
    
    @Column(name = "quantity", nullable = false)
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    @Positive(message = "Total price must be positive")
    private BigDecimal totalPrice;
    
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "weight")
    private BigDecimal weight;
    
    @Column(name = "dimensions")
    private String dimensions;
    
    @Column(name = "is_digital")
    private Boolean isDigital = false;
    
    @Column(name = "is_gift_wrap")
    private Boolean isGiftWrap = false;
    
    @Column(name = "gift_message", columnDefinition = "TEXT")
    private String giftMessage;
    
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, String sku, 
                    Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
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
        recalculateTotalPrice();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateTotalPrice();
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
    
    // Helper methods
    private void recalculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public BigDecimal getFinalPrice() {
        return totalPrice.add(taxAmount).subtract(discountAmount);
    }
    
    public boolean isPhysicalProduct() {
        return !Boolean.TRUE.equals(isDigital);
    }
    
    public boolean requiresShipping() {
        return isPhysicalProduct();
    }
    
    public BigDecimal getTotalWeight() {
        if (weight != null && quantity != null) {
            return weight.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id) &&
               Objects.equals(productId, orderItem.productId) &&
               Objects.equals(sku, orderItem.sku);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, productId, sku);
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", isDigital=" + isDigital +
                '}';
    }
}