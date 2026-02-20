package com.ecommerce.order.exception;

import java.time.Instant;

public class RateLimitExceededException extends RuntimeException {
    
    private final String key;
    private final Integer limit;
    private final Integer current;
    private final Instant resetTime;
    
    public RateLimitExceededException(String message) {
        super(message);
        this.key = null;
        this.limit = null;
        this.current = null;
        this.resetTime = null;
    }
    
    public RateLimitExceededException(String message, String key) {
        super(message);
        this.key = key;
        this.limit = null;
        this.current = null;
        this.resetTime = null;
    }
    
    public RateLimitExceededException(String message, String key, Integer limit, Integer current, Instant resetTime) {
        super(message);
        this.key = key;
        this.limit = limit;
        this.current = current;
        this.resetTime = resetTime;
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.key = null;
        this.limit = null;
        this.current = null;
        this.resetTime = null;
    }
    
    public String getKey() {
        return key;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public Integer getCurrent() {
        return current;
    }
    
    public Instant getResetTime() {
        return resetTime;
    }
}