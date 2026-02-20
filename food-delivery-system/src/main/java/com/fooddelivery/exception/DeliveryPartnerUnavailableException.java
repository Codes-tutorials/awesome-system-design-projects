package com.fooddelivery.exception;

/**
 * Exception thrown when no delivery partner is available for an order
 */
public class DeliveryPartnerUnavailableException extends RuntimeException {
    
    private final int estimatedWaitTime;
    
    public DeliveryPartnerUnavailableException(String message, int estimatedWaitTime) {
        super(message);
        this.estimatedWaitTime = estimatedWaitTime;
    }
    
    public DeliveryPartnerUnavailableException(String message, int estimatedWaitTime, Throwable cause) {
        super(message, cause);
        this.estimatedWaitTime = estimatedWaitTime;
    }
    
    public int getEstimatedWaitTime() {
        return estimatedWaitTime;
    }
}