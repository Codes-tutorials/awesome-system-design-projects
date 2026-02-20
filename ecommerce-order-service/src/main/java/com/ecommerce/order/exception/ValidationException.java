package com.ecommerce.order.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    
    private final List<String> validationErrors;
    private final String field;
    
    public ValidationException(String message) {
        super(message);
        this.validationErrors = null;
        this.field = null;
    }
    
    public ValidationException(String message, String field) {
        super(message);
        this.validationErrors = null;
        this.field = field;
    }
    
    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
        this.field = null;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.validationErrors = null;
        this.field = null;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
    
    public String getField() {
        return field;
    }
}