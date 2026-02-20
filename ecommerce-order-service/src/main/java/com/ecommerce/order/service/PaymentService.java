package com.ecommerce.order.service;

import com.ecommerce.order.exception.PaymentFailedException;
import com.ecommerce.order.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    
    private final WebClient webClient;
    private final Random random = new Random();
    
    @Autowired
    public PaymentService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://payment-service:8082") // External payment service URL
                .build();
    }
    
    /**
     * Process payment for order with circuit breaker
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "processPaymentFallback")
    @Retry(name = "payment-service")
    @TimeLimiter(name = "payment-service")
    public CompletableFuture<String> processPaymentAsync(Order order) {
        log.info("Processing payment for order: {}, amount: {}", order.getOrderId(), order.getTotalAmount());
        
        PaymentRequest request = createPaymentRequest(order);
        
        return webClient.post()
                .uri("/payments/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .timeout(Duration.ofSeconds(5))
                .map(response -> {
                    if (response.isSuccess()) {
                        log.info("Payment successful for order: {}, paymentId: {}", 
                                order.getOrderId(), response.getPaymentId());
                        return response.getPaymentId();
                    } else {
                        log.error("Payment failed for order: {}, error: {}", 
                                 order.getOrderId(), response.getErrorMessage());
                        throw new PaymentFailedException(
                            response.getErrorMessage(),
                            order.getOrderId(),
                            response.getPaymentId(),
                            response.getErrorCode()
                        );
                    }
                })
                .toFuture();
    }
    
    /**
     * Synchronous version for backward compatibility
     */
    public String processPayment(Order order) {
        try {
            return processPaymentAsync(order).get();
        } catch (Exception e) {
            log.error("Payment processing failed for order: {}", order.getOrderId(), e);
            throw new PaymentFailedException(
                "Payment processing failed", 
                order.getOrderId(), 
                null, 
                "PROCESSING_ERROR"
            );
        }
    }
    
    /**
     * Fallback method for payment processing
     */
    public CompletableFuture<String> processPaymentFallback(Order order, Exception ex) {
        log.warn("Payment service unavailable, using fallback for order: {}", order.getOrderId(), ex);
        
        // In fallback, simulate payment processing
        String paymentId = simulatePaymentProcessing(order);
        
        if (paymentId != null) {
            log.info("Fallback payment successful for order: {}, paymentId: {}", order.getOrderId(), paymentId);
            return CompletableFuture.completedFuture(paymentId);
        } else {
            log.error("Fallback payment failed for order: {}", order.getOrderId());
            throw new PaymentFailedException(
                "Payment service unavailable and fallback failed",
                order.getOrderId(),
                null,
                "SERVICE_UNAVAILABLE"
            );
        }
    }
    
    /**
     * Process refund for cancelled order
     */
    @CircuitBreaker(name = "payment-service", fallbackMethod = "processRefundFallback")
    @Retry(name = "payment-service")
    public void processRefund(String paymentId, BigDecimal amount) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
        
        RefundRequest request = new RefundRequest(paymentId, amount);
        
        try {
            RefundResponse response = webClient.post()
                    .uri("/payments/refund")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RefundResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            if (response != null && response.isSuccess()) {
                log.info("Refund successful for payment: {}, refundId: {}", paymentId, response.getRefundId());
            } else {
                log.error("Refund failed for payment: {}, error: {}", 
                         paymentId, response != null ? response.getErrorMessage() : "Unknown error");
                throw new PaymentFailedException(
                    "Refund processing failed",
                    null,
                    paymentId,
                    response != null ? response.getErrorCode() : "REFUND_FAILED"
                );
            }
            
        } catch (Exception e) {
            log.error("Refund processing failed for payment: {}", paymentId, e);
            throw new PaymentFailedException("Refund processing failed", null, paymentId, "PROCESSING_ERROR");
        }
    }
    
    /**
     * Fallback method for refund processing
     */
    public void processRefundFallback(String paymentId, BigDecimal amount, Exception ex) {
        log.warn("Payment service unavailable for refund, using fallback for payment: {}", paymentId, ex);
        
        // In fallback, log the refund request for manual processing
        log.warn("Manual refund required - Payment ID: {}, Amount: {}", paymentId, amount);
        
        // Could also queue the refund request for later processing
        // queueRefundForLaterProcessing(paymentId, amount);
    }
    
    /**
     * Validate payment method
     */
    public boolean validatePaymentMethod(String paymentMethod, String userId) {
        log.debug("Validating payment method: {} for user: {}", paymentMethod, userId);
        
        try {
            PaymentMethodValidationRequest request = new PaymentMethodValidationRequest(paymentMethod, userId);
            
            PaymentMethodValidationResponse response = webClient.post()
                    .uri("/payments/validate-method")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentMethodValidationResponse.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
            
            boolean valid = response != null && response.isValid();
            log.debug("Payment method validation result: {}", valid);
            return valid;
            
        } catch (Exception e) {
            log.error("Payment method validation failed for user: {}", userId, e);
            // Fail open - assume valid if service is unavailable
            return true;
        }
    }
    
    /**
     * Get payment status
     */
    public PaymentStatus getPaymentStatus(String paymentId) {
        log.debug("Getting payment status for payment: {}", paymentId);
        
        try {
            PaymentStatusResponse response = webClient.get()
                    .uri("/payments/{paymentId}/status", paymentId)
                    .retrieve()
                    .bodyToMono(PaymentStatusResponse.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
            
            if (response != null) {
                return PaymentStatus.valueOf(response.getStatus());
            }
            
            return PaymentStatus.UNKNOWN;
            
        } catch (Exception e) {
            log.error("Failed to get payment status for payment: {}", paymentId, e);
            return PaymentStatus.UNKNOWN;
        }
    }
    
    /**
     * Create payment request from order
     */
    private PaymentRequest createPaymentRequest(Order order) {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(order.getOrderId());
        request.setUserId(order.getUserId());
        request.setAmount(order.getTotalAmount());
        request.setCurrency(order.getCurrency());
        request.setPaymentMethod(order.getPaymentMethod());
        request.setDescription("Payment for order " + order.getOrderNumber());
        
        // Add metadata
        request.addMetadata("orderNumber", order.getOrderNumber());
        request.addMetadata("itemCount", String.valueOf(order.getItems().size()));
        request.addMetadata("source", order.getSource());
        
        if (Boolean.TRUE.equals(order.getIsFlashSale())) {
            request.addMetadata("flashSale", "true");
            request.addMetadata("flashSaleId", order.getFlashSaleId());
        }
        
        return request;
    }
    
    /**
     * Simulate payment processing (for demo/testing purposes)
     */
    private String simulatePaymentProcessing(Order order) {
        // Simulate 95% success rate
        boolean success = random.nextDouble() < 0.95;
        
        if (success) {
            String paymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            log.debug("Simulated successful payment for order: {}, paymentId: {}", order.getOrderId(), paymentId);
            return paymentId;
        } else {
            log.debug("Simulated payment failure for order: {}", order.getOrderId());
            return null;
        }
    }
    
    // Payment status enum
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED,
        UNKNOWN
    }
    
    // DTOs for external service communication
    public static class PaymentRequest {
        private String orderId;
        private String userId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String description;
        private java.util.Map<String, String> metadata = new java.util.HashMap<>();
        
        // Constructors
        public PaymentRequest() {}
        
        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public java.util.Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(java.util.Map<String, String> metadata) { this.metadata = metadata; }
        
        public void addMetadata(String key, String value) { this.metadata.put(key, value); }
    }
    
    public static class PaymentResponse {
        private boolean success;
        private String paymentId;
        private String status;
        private String errorCode;
        private String errorMessage;
        private LocalDateTime processedAt;
        
        // Constructors
        public PaymentResponse() {}
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
    
    public static class RefundRequest {
        private String paymentId;
        private BigDecimal amount;
        private String reason;
        
        public RefundRequest() {}
        
        public RefundRequest(String paymentId, BigDecimal amount) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.reason = "Order cancellation";
        }
        
        // Getters and Setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class RefundResponse {
        private boolean success;
        private String refundId;
        private String status;
        private String errorCode;
        private String errorMessage;
        private LocalDateTime processedAt;
        
        // Constructors
        public RefundResponse() {}
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
    
    public static class PaymentMethodValidationRequest {
        private String paymentMethod;
        private String userId;
        
        public PaymentMethodValidationRequest() {}
        
        public PaymentMethodValidationRequest(String paymentMethod, String userId) {
            this.paymentMethod = paymentMethod;
            this.userId = userId;
        }
        
        // Getters and Setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class PaymentMethodValidationResponse {
        private boolean valid;
        private String message;
        
        // Constructors
        public PaymentMethodValidationResponse() {}
        
        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class PaymentStatusResponse {
        private String status;
        private String paymentId;
        private LocalDateTime lastUpdated;
        
        // Constructors
        public PaymentStatusResponse() {}
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}