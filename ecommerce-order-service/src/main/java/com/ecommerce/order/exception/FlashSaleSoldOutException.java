package com.ecommerce.order.exception;

public class FlashSaleSoldOutException extends RuntimeException {
    
    private final String flashSaleId;
    private final String productId;
    
    public FlashSaleSoldOutException(String message) {
        super(message);
        this.flashSaleId = null;
        this.productId = null;
    }
    
    public FlashSaleSoldOutException(String message, String flashSaleId) {
        super(message);
        this.flashSaleId = flashSaleId;
        this.productId = null;
    }
    
    public FlashSaleSoldOutException(String message, String flashSaleId, String productId) {
        super(message);
        this.flashSaleId = flashSaleId;
        this.productId = productId;
    }
    
    public FlashSaleSoldOutException(String message, Throwable cause) {
        super(message, cause);
        this.flashSaleId = null;
        this.productId = null;
    }
    
    public String getFlashSaleId() {
        return flashSaleId;
    }
    
    public String getProductId() {
        return productId;
    }
}