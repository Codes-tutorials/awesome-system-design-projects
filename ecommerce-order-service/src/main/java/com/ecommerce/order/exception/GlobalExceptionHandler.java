package com.ecommerce.order.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle order not found exceptions
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        log.warn("Order not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("ORDER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(ex.getOrderId() != null ? Map.of("orderId", ex.getOrderId()) : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle insufficient inventory exceptions
     */
    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientInventory(InsufficientInventoryException ex, WebRequest request) {
        log.warn("Insufficient inventory: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getProductId() != null) {
            details.put("productId", ex.getProductId());
        }
        if (ex.getRequestedQuantity() != null) {
            details.put("requestedQuantity", ex.getRequestedQuantity());
        }
        if (ex.getAvailableQuantity() != null) {
            details.put("availableQuantity", ex.getAvailableQuantity());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INSUFFICIENT_INVENTORY")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details.isEmpty() ? null : details)
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle payment failed exceptions
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex, WebRequest request) {
        log.error("Payment failed: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getOrderId() != null) {
            details.put("orderId", ex.getOrderId());
        }
        if (ex.getPaymentId() != null) {
            details.put("paymentId", ex.getPaymentId());
        }
        if (ex.getErrorCode() != null) {
            details.put("errorCode", ex.getErrorCode());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("PAYMENT_FAILED")
                .message("Payment processing failed")
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details.isEmpty() ? null : details)
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }
    
    /**
     * Handle rate limit exceeded exceptions
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getKey() != null) {
            details.put("rateLimitKey", ex.getKey());
        }
        if (ex.getLimit() != null) {
            details.put("limit", ex.getLimit());
        }
        if (ex.getCurrent() != null) {
            details.put("current", ex.getCurrent());
        }
        if (ex.getResetTime() != null) {
            details.put("resetTime", ex.getResetTime());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Please try again later.")
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details.isEmpty() ? null : details)
                .retryAfter(ex.getResetTime() != null ? 
                           (int) (ex.getResetTime().getEpochSecond() - Instant.now().getEpochSecond()) : 60)
                .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, WebRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getField() != null) {
            details.put("field", ex.getField());
        }
        if (ex.getValidationErrors() != null) {
            details.put("validationErrors", ex.getValidationErrors());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details.isEmpty() ? null : details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle flash sale not active exceptions
     */
    @ExceptionHandler(FlashSaleNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleFlashSaleNotActive(FlashSaleNotActiveException ex, WebRequest request) {
        log.warn("Flash sale not active: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("FLASH_SALE_NOT_ACTIVE")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(ex.getFlashSaleId() != null ? Map.of("flashSaleId", ex.getFlashSaleId()) : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle flash sale sold out exceptions
     */
    @ExceptionHandler(FlashSaleSoldOutException.class)
    public ResponseEntity<ErrorResponse> handleFlashSaleSoldOut(FlashSaleSoldOutException ex, WebRequest request) {
        log.warn("Flash sale sold out: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getFlashSaleId() != null) {
            details.put("flashSaleId", ex.getFlashSaleId());
        }
        if (ex.getProductId() != null) {
            details.put("productId", ex.getProductId());
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .code("FLASH_SALE_SOLD_OUT")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details.isEmpty() ? null : details)
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle method argument validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Method argument validation failed: {}", ex.getMessage());
        
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        Map<String, Object> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
        
        Map<String, Object> details = new HashMap<>();
        details.put("validationErrors", validationErrors);
        details.put("fieldErrors", fieldErrors);
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message("Request validation failed")
                .timestamp(Instant.now())
                .path(getPath(request))
                .details(details)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("ACCESS_DENIED")
                .message("Access denied")
                .timestamp(Instant.now())
                .path(getPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("ILLEGAL_STATE")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .path(getPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .path(getPath(request))
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Extract path from web request
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
    
    /**
     * Error response model
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        private Instant timestamp;
        private String path;
        private Map<String, Object> details;
        private Integer retryAfter;
        
        // Constructors
        public ErrorResponse() {}
        
        private ErrorResponse(Builder builder) {
            this.code = builder.code;
            this.message = builder.message;
            this.timestamp = builder.timestamp;
            this.path = builder.path;
            this.details = builder.details;
            this.retryAfter = builder.retryAfter;
        }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String code;
            private String message;
            private Instant timestamp;
            private String path;
            private Map<String, Object> details;
            private Integer retryAfter;
            
            public Builder code(String code) {
                this.code = code;
                return this;
            }
            
            public Builder message(String message) {
                this.message = message;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public Builder path(String path) {
                this.path = path;
                return this;
            }
            
            public Builder details(Map<String, Object> details) {
                this.details = details;
                return this;
            }
            
            public Builder retryAfter(Integer retryAfter) {
                this.retryAfter = retryAfter;
                return this;
            }
            
            public ErrorResponse build() {
                return new ErrorResponse(this);
            }
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
        
        public Integer getRetryAfter() { return retryAfter; }
        public void setRetryAfter(Integer retryAfter) { this.retryAfter = retryAfter; }
    }
}