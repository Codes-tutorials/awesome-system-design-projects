package com.fooddelivery.exception;

/**
 * Exception thrown when there is insufficient inventory for an order
 */
public class InsufficientInventoryException extends RuntimeException {
    
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientInventoryException(String message, int requestedQuantity, int availableQuantity) {
        super(message);
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientInventoryException(String message, int requestedQuantity, int availableQuantity, Throwable cause) {
        super(message, cause);
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}