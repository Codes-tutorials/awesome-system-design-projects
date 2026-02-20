package com.ecommerce.order.service;

import com.ecommerce.order.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    private final WebClient webClient;
    
    @Autowired
    public NotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://notification-service:8083") // External notification service URL
                .build();
    }
    
    /**
     * Send order confirmation notification
     */
    @Async("notificationExecutor")
    public void sendOrderConfirmation(Order order) {
        log.info("Sending order confirmation notification for order: {}", order.getOrderId());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_CONFIRMATION")
                .channel("EMAIL")
                .subject("Order Confirmation - " + order.getOrderNumber())
                .templateId("order_confirmation")
                .addData("orderNumber", order.getOrderNumber())
                .addData("totalAmount", order.getTotalAmount().toString())
                .addData("currency", order.getCurrency())
                .addData("itemCount", String.valueOf(order.getItems().size()))
                .addData("orderDate", order.getCreatedAt().toString())
                .build();
        
        sendNotificationAsync(request);
    }
    
    /**
     * Send order shipped notification
     */
    @Async("notificationExecutor")
    public void sendOrderShipped(Order order) {
        log.info("Sending order shipped notification for order: {}", order.getOrderId());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_SHIPPED")
                .channel("EMAIL")
                .subject("Your Order Has Been Shipped - " + order.getOrderNumber())
                .templateId("order_shipped")
                .addData("orderNumber", order.getOrderNumber())
                .addData("shippedDate", order.getShippedAt().toString())
                .addData("trackingNumber", generateTrackingNumber(order))
                .build();
        
        sendNotificationAsync(request);
        
        // Also send SMS notification for shipped orders
        NotificationRequest smsRequest = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_SHIPPED")
                .channel("SMS")
                .templateId("order_shipped_sms")
                .addData("orderNumber", order.getOrderNumber())
                .addData("trackingNumber", generateTrackingNumber(order))
                .build();
        
        sendNotificationAsync(smsRequest);
    }
    
    /**
     * Send order delivered notification
     */
    @Async("notificationExecutor")
    public void sendOrderDelivered(Order order) {
        log.info("Sending order delivered notification for order: {}", order.getOrderId());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_DELIVERED")
                .channel("EMAIL")
                .subject("Order Delivered - " + order.getOrderNumber())
                .templateId("order_delivered")
                .addData("orderNumber", order.getOrderNumber())
                .addData("deliveredDate", order.getDeliveredAt().toString())
                .build();
        
        sendNotificationAsync(request);
        
        // Send push notification for delivery
        NotificationRequest pushRequest = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_DELIVERED")
                .channel("PUSH")
                .title("Order Delivered!")
                .message("Your order " + order.getOrderNumber() + " has been delivered.")
                .build();
        
        sendNotificationAsync(pushRequest);
    }
    
    /**
     * Send order cancellation notification
     */
    @Async("notificationExecutor")
    public void sendOrderCancellation(Order order, String reason) {
        log.info("Sending order cancellation notification for order: {}", order.getOrderId());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("ORDER_CANCELLED")
                .channel("EMAIL")
                .subject("Order Cancelled - " + order.getOrderNumber())
                .templateId("order_cancelled")
                .addData("orderNumber", order.getOrderNumber())
                .addData("cancelledDate", order.getCancelledAt().toString())
                .addData("reason", reason)
                .addData("refundAmount", order.getTotalAmount().toString())
                .build();
        
        sendNotificationAsync(request);
    }
    
    /**
     * Send flash sale order notification
     */
    @Async("notificationExecutor")
    public void sendFlashSaleOrderNotification(Order order, String status) {
        log.info("Sending flash sale order notification for order: {}, status: {}", order.getOrderId(), status);
        
        String templateId = switch (status) {
            case "QUEUED" -> "flash_sale_queued";
            case "PROCESSING" -> "flash_sale_processing";
            case "CONFIRMED" -> "flash_sale_confirmed";
            case "FAILED" -> "flash_sale_failed";
            default -> "flash_sale_update";
        };
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("FLASH_SALE_UPDATE")
                .channel("PUSH")
                .title("Flash Sale Order Update")
                .message("Your flash sale order " + order.getOrderNumber() + " is " + status.toLowerCase())
                .templateId(templateId)
                .addData("orderNumber", order.getOrderNumber())
                .addData("flashSaleId", order.getFlashSaleId())
                .addData("status", status)
                .build();
        
        sendNotificationAsync(request);
    }
    
    /**
     * Send payment failure notification
     */
    @Async("notificationExecutor")
    public void sendPaymentFailureNotification(Order order, String errorMessage) {
        log.info("Sending payment failure notification for order: {}", order.getOrderId());
        
        NotificationRequest request = NotificationRequest.builder()
                .userId(order.getUserId())
                .type("PAYMENT_FAILED")
                .channel("EMAIL")
                .subject("Payment Failed - " + order.getOrderNumber())
                .templateId("payment_failed")
                .addData("orderNumber", order.getOrderNumber())
                .addData("errorMessage", errorMessage)
                .addData("totalAmount", order.getTotalAmount().toString())
                .addData("currency", order.getCurrency())
                .build();
        
        sendNotificationAsync(request);
    }
    
    /**
     * Send low inventory alert (for admin)
     */
    @Async("notificationExecutor")
    public void sendLowInventoryAlert(String productId, int currentStock, int threshold) {
        log.info("Sending low inventory alert for product: {}, stock: {}", productId, currentStock);
        
        NotificationRequest request = NotificationRequest.builder()
                .userId("admin") // Send to admin users
                .type("LOW_INVENTORY_ALERT")
                .channel("EMAIL")
                .subject("Low Inventory Alert - Product " + productId)
                .templateId("low_inventory_alert")
                .addData("productId", productId)
                .addData("currentStock", String.valueOf(currentStock))
                .addData("threshold", String.valueOf(threshold))
                .build();
        
        sendNotificationAsync(request);
    }
    
    /**
     * Send notification asynchronously
     */
    private void sendNotificationAsync(NotificationRequest request) {
        try {
            webClient.post()
                    .uri("/notifications/send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NotificationResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .subscribe(
                        response -> {
                            if (response.isSuccess()) {
                                log.debug("Notification sent successfully: {}", response.getNotificationId());
                            } else {
                                log.warn("Notification failed: {}", response.getErrorMessage());
                            }
                        },
                        error -> {
                            log.error("Failed to send notification", error);
                            // Could implement retry logic or dead letter queue here
                        }
                    );
                    
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
    
    /**
     * Generate tracking number for shipped orders
     */
    private String generateTrackingNumber(Order order) {
        // Simple tracking number generation
        return "TRK" + order.getOrderNumber().replace("ORD-", "") + "001";
    }
    
    // DTOs for notification service communication
    public static class NotificationRequest {
        private String userId;
        private String type;
        private String channel;
        private String subject;
        private String title;
        private String message;
        private String templateId;
        private Map<String, String> data = new HashMap<>();
        private String priority = "NORMAL";
        
        // Constructors
        public NotificationRequest() {}
        
        private NotificationRequest(Builder builder) {
            this.userId = builder.userId;
            this.type = builder.type;
            this.channel = builder.channel;
            this.subject = builder.subject;
            this.title = builder.title;
            this.message = builder.message;
            this.templateId = builder.templateId;
            this.data = builder.data;
            this.priority = builder.priority;
        }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String userId;
            private String type;
            private String channel;
            private String subject;
            private String title;
            private String message;
            private String templateId;
            private Map<String, String> data = new HashMap<>();
            private String priority = "NORMAL";
            
            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }
            
            public Builder type(String type) {
                this.type = type;
                return this;
            }
            
            public Builder channel(String channel) {
                this.channel = channel;
                return this;
            }
            
            public Builder subject(String subject) {
                this.subject = subject;
                return this;
            }
            
            public Builder title(String title) {
                this.title = title;
                return this;
            }
            
            public Builder message(String message) {
                this.message = message;
                return this;
            }
            
            public Builder templateId(String templateId) {
                this.templateId = templateId;
                return this;
            }
            
            public Builder addData(String key, String value) {
                this.data.put(key, value);
                return this;
            }
            
            public Builder priority(String priority) {
                this.priority = priority;
                return this;
            }
            
            public NotificationRequest build() {
                return new NotificationRequest(this);
            }
        }
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }
        
        public Map<String, String> getData() { return data; }
        public void setData(Map<String, String> data) { this.data = data; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    public static class NotificationResponse {
        private boolean success;
        private String notificationId;
        private String status;
        private String errorMessage;
        private LocalDateTime sentAt;
        
        // Constructors
        public NotificationResponse() {}
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getNotificationId() { return notificationId; }
        public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    }
}