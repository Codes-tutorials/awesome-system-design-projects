package com.fooddelivery.exception;

/**
 * Exception thrown when an invalid order status transition is attempted
 */
public class InvalidOrderStatusException extends RuntimeException {
    
    private final String currentStatus;
    private final String attemptedStatus;
    
    public InvalidOrderStatusException(String message, String currentStatus, String attemptedStatus) {
        super(message);
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    
    public InvalidOrderStatusException(String message, String currentStatus, String attemptedStatus, Throwable cause) {
        super(message, cause);
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public String getAttemptedStatus() {
        return attemptedStatus;
    }
}