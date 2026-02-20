package com.fooddelivery.service;

import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service class for handling notifications (Email, SMS, Push)
 */
@Service
public class NotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    // Email notifications
    public void sendVerificationEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Verify Your Account - Food Delivery");
            message.setText("Please click the link to verify your account: " +
                    "http://localhost:8080/api/auth/verify?token=" + user.getVerificationToken());
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
    }
    
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset - Food Delivery");
            message.setText("Please click the link to reset your password: " +
                    "http://localhost:8080/reset-password?token=" + resetToken);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }
    
    public void sendOrderConfirmation(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("Order Confirmation - " + order.getOrderNumber());
            message.setText("Your order has been placed successfully!\n\n" +
                    "Order Number: " + order.getOrderNumber() + "\n" +
                    "Restaurant: " + order.getRestaurant().getName() + "\n" +
                    "Total Amount: $" + order.getTotalAmount() + "\n" +
                    "Estimated Delivery: " + order.getEstimatedDeliveryTime());
            
            mailSender.send(message);
            
            // Send push notification via Kafka
            kafkaTemplate.send("order-notifications", createOrderNotification(order, "ORDER_PLACED"));
            
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation: " + e.getMessage());
        }
    }
    
    public void sendOrderStatusUpdate(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("Order Update - " + order.getOrderNumber());
            message.setText("Your order status has been updated to: " + order.getStatus() + "\n\n" +
                    "Order Number: " + order.getOrderNumber() + "\n" +
                    "Restaurant: " + order.getRestaurant().getName());
            
            mailSender.send(message);
            
            // Send push notification
            kafkaTemplate.send("order-notifications", createOrderNotification(order, "ORDER_STATUS_UPDATE"));
            
        } catch (Exception e) {
            System.err.println("Failed to send order status update: " + e.getMessage());
        }
    }
    
    public void notifyRestaurantNewOrder(Order order) {
        try {
            // Send email to restaurant
            if (order.getRestaurant().getEmail() != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(order.getRestaurant().getEmail());
                message.setSubject("New Order - " + order.getOrderNumber());
                message.setText("You have received a new order!\n\n" +
                        "Order Number: " + order.getOrderNumber() + "\n" +
                        "Customer: " + order.getCustomer().getFullName() + "\n" +
                        "Total Amount: $" + order.getTotalAmount() + "\n" +
                        "Delivery Address: " + order.getDeliveryAddress());
                
                mailSender.send(message);
            }
            
            // Send push notification to restaurant owner
            kafkaTemplate.send("restaurant-notifications", createRestaurantNotification(order, "NEW_ORDER"));
            
        } catch (Exception e) {
            System.err.println("Failed to notify restaurant of new order: " + e.getMessage());
        }
    }
    
    public void notifyDeliveryPartnerNewOrder(Order order) {
        try {
            if (order.getDeliveryPartner() != null) {
                // Send push notification to delivery partner
                kafkaTemplate.send("delivery-notifications", createDeliveryNotification(order, "NEW_DELIVERY"));
            }
        } catch (Exception e) {
            System.err.println("Failed to notify delivery partner of new order: " + e.getMessage());
        }
    }
    
    public void requestOrderReview(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomer().getEmail());
            message.setSubject("Rate Your Order - " + order.getOrderNumber());
            message.setText("Your order has been delivered! Please take a moment to rate your experience.\n\n" +
                    "Order Number: " + order.getOrderNumber() + "\n" +
                    "Restaurant: " + order.getRestaurant().getName() + "\n" +
                    "Rate your order: http://localhost:8080/orders/" + order.getId() + "/review");
            
            mailSender.send(message);
            
            // Send push notification
            kafkaTemplate.send("order-notifications", createOrderNotification(order, "REQUEST_REVIEW"));
            
        } catch (Exception e) {
            System.err.println("Failed to send review request: " + e.getMessage());
        }
    }
    
    public void sendDeliveryPartnerVerification(DeliveryPartner deliveryPartner) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(deliveryPartner.getUser().getEmail());
            message.setSubject("Account Verified - Food Delivery Partner");
            message.setText("Congratulations! Your delivery partner account has been verified.\n\n" +
                    "Partner ID: " + deliveryPartner.getPartnerId() + "\n" +
                    "You can now start accepting delivery orders.");
            
            mailSender.send(message);
            
            // Send push notification
            kafkaTemplate.send("delivery-notifications", createPartnerNotification(deliveryPartner, "ACCOUNT_VERIFIED"));
            
        } catch (Exception e) {
            System.err.println("Failed to send delivery partner verification: " + e.getMessage());
        }
    }
    
    // SMS notifications (using Twilio - implementation would go here)
    public void sendOrderSMS(Order order, String message) {
        // Implementation for SMS using Twilio SDK
        System.out.println("SMS to " + order.getCustomer().getPhoneNumber() + ": " + message);
    }
    
    // Push notifications (using Firebase - implementation would go here)
    public void sendPushNotification(String deviceToken, String title, String body) {
        // Implementation for push notifications using Firebase SDK
        System.out.println("Push notification - Title: " + title + ", Body: " + body);
    }
    
    // Helper methods to create notification objects
    private Object createOrderNotification(Order order, String type) {
        return new Object() {
            public final String notificationType = type;
            public final String orderId = order.getId().toString();
            public final String orderNumber = order.getOrderNumber();
            public final String customerId = order.getCustomer().getId().toString();
            public final String status = order.getStatus().toString();
            public final String restaurantName = order.getRestaurant().getName();
        };
    }
    
    private Object createRestaurantNotification(Order order, String type) {
        return new Object() {
            public final String notificationType = type;
            public final String orderId = order.getId().toString();
            public final String orderNumber = order.getOrderNumber();
            public final String restaurantId = order.getRestaurant().getId().toString();
            public final String customerName = order.getCustomer().getFullName();
            public final String totalAmount = order.getTotalAmount().toString();
        };
    }
    
    private Object createDeliveryNotification(Order order, String type) {
        return new Object() {
            public final String notificationType = type;
            public final String orderId = order.getId().toString();
            public final String orderNumber = order.getOrderNumber();
            public final String deliveryPartnerId = order.getDeliveryPartner().getId().toString();
            public final String restaurantName = order.getRestaurant().getName();
            public final String deliveryAddress = order.getDeliveryAddress();
        };
    }
    
    private Object createPartnerNotification(DeliveryPartner partner, String type) {
        return new Object() {
            public final String notificationType = type;
            public final String partnerId = partner.getPartnerId();
            public final String deliveryPartnerId = partner.getId().toString();
            public final String partnerName = partner.getUser().getFullName();
        };
    }
}