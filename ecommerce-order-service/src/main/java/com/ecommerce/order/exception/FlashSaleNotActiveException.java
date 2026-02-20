package com.ecommerce.order.exception;

public class FlashSaleNotActiveException extends RuntimeException {
    
    private final String flashSaleId;
    
    public FlashSaleNotActiveException(String message) {
        super(message);
        this.flashSaleId = null;
    }
    
    public FlashSaleNotActiveException(String message, String flashSaleId) {
        super(message);
        this.flashSaleId = flashSaleId;
    }
    
    public FlashSaleNotActiveException(String message, Throwable cause) {
        super(message, cause);
        this.flashSaleId = null;
    }
    
    public String getFlashSaleId() {
        return flashSaleId;
    }
}