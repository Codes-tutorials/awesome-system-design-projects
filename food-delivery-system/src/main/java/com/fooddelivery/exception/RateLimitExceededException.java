package com.fooddelivery.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final long retryAfter;
    
    public RateLimitExceededException(String message, long retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
    
    public RateLimitExceededException(String message, long retryAfter, Throwable cause) {
        super(message, cause);
        this.retryAfter = retryAfter;
    }
    
    public long getRetryAfter() {
        return retryAfter;
    }
}