package com.ecommerce.order.exception;

public class PaymentFailedException extends RuntimeException {
    
    private final String orderId;
    private final String paymentId;
    private final String errorCode;
    
    public PaymentFailedException(String message) {
        super(message);
        this.orderId = null;
        this.paymentId = null;
        this.errorCode = null;
    }
    
    public PaymentFailedException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
        this.paymentId = null;
        this.errorCode = null;
    }
    
    public PaymentFailedException(String message, String orderId, String paymentId) {
        super(message);
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.errorCode = null;
    }
    
    public PaymentFailedException(String message, String orderId, String paymentId, String errorCode) {
        super(message);
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.errorCode = errorCode;
    }
    
    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.paymentId = null;
        this.errorCode = null;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}