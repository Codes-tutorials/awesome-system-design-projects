package com.fooddelivery.dto;

import com.fooddelivery.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new order
 */
public class CreateOrderRequest {
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    private Double deliveryLatitude;
    
    private Double deliveryLongitude;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private String specialInstructions;
    
    // Constructors
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(Long restaurantId, String deliveryAddress, 
                            PaymentMethod paymentMethod) {
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and Setters
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public Double getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(Double deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }
    
    public Double getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(Double deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}