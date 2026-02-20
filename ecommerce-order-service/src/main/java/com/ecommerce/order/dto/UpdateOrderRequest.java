package com.ecommerce.order.dto;

import jakarta.validation.constraints.Size;

public class UpdateOrderRequest {
    
    private String shippingAddressId;
    
    private String billingAddressId;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    private String paymentMethod;
    
    // Constructors
    public UpdateOrderRequest() {}
    
    public UpdateOrderRequest(String shippingAddressId, String billingAddressId, String notes) {
        this.shippingAddressId = shippingAddressId;
        this.billingAddressId = billingAddressId;
        this.notes = notes;
    }
    
    // Getters and Setters
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    @Override
    public String toString() {
        return "UpdateOrderRequest{" +
                "shippingAddressId='" + shippingAddressId + '\'' +
                ", billingAddressId='" + billingAddressId + '\'' +
                ", notes='" + notes + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}