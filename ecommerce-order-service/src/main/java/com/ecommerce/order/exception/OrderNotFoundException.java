package com.ecommerce.order.exception;

public class OrderNotFoundException extends RuntimeException {
    
    private final String orderId;
    
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
    }
    
    public OrderNotFoundException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
    }
    
    public OrderNotFoundException(String message, String orderId, Throwable cause) {
        super(message, cause);
        this.orderId = orderId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}