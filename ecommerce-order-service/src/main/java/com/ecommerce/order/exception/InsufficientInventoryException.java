package com.ecommerce.order.exception;

public class InsufficientInventoryException extends RuntimeException {
    
    private final String productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientInventoryException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public InsufficientInventoryException(String message, String productId) {
        super(message);
        this.productId = productId;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public InsufficientInventoryException(String message, String productId, Integer requestedQuantity, Integer availableQuantity) {
        super(message);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientInventoryException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}