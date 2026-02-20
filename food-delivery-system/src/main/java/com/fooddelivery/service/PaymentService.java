package com.fooddelivery.service;

import com.fooddelivery.model.Order;
import com.fooddelivery.model.PaymentMethod;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling payment processing
 */
@Service
public class PaymentService {
    
    @Value("${stripe.secret.key:sk_test_dummy}")
    private String stripeSecretKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    public String processPayment(Order order) throws Exception {
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            return "COD_" + System.currentTimeMillis();
        }
        
        try {
            // Convert amount to cents for Stripe
            long amountInCents = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setDescription("Food Delivery Order - " + order.getOrderNumber())
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("order_number", order.getOrderNumber())
                    .putMetadata("customer_id", order.getCustomer().getId().toString())
                    .putMetadata("restaurant_id", order.getRestaurant().getId().toString())
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // In a real implementation, you would return the client_secret to the frontend
            // and handle the payment confirmation there. For this demo, we'll simulate success.
            return paymentIntent.getId();
            
        } catch (StripeException e) {
            throw new Exception("Payment processing failed: " + e.getMessage());
        }
    }
    
    public String processRefund(Order order) throws Exception {
        if (order.getPaymentTransactionId() == null) {
            throw new Exception("No payment transaction found for this order");
        }
        
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            return "COD_REFUND_" + System.currentTimeMillis();
        }
        
        try {
            // Convert amount to cents for Stripe
            long amountInCents = order.getTotalAmount().multiply(new BigDecimal("100")).longValue();
            
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(order.getPaymentTransactionId())
                    .setAmount(amountInCents)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("order_number", order.getOrderNumber())
                    .build();
            
            Refund refund = Refund.create(params);
            return refund.getId();
            
        } catch (StripeException e) {
            throw new Exception("Refund processing failed: " + e.getMessage());
        }
    }
    
    public String processPartialRefund(Order order, BigDecimal refundAmount) throws Exception {
        if (order.getPaymentTransactionId() == null) {
            throw new Exception("No payment transaction found for this order");
        }
        
        if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
            throw new Exception("Refund amount cannot be greater than order total");
        }
        
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            return "COD_PARTIAL_REFUND_" + System.currentTimeMillis();
        }
        
        try {
            // Convert amount to cents for Stripe
            long amountInCents = refundAmount.multiply(new BigDecimal("100")).longValue();
            
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(order.getPaymentTransactionId())
                    .setAmount(amountInCents)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("order_number", order.getOrderNumber())
                    .putMetadata("refund_type", "partial")
                    .build();
            
            Refund refund = Refund.create(params);
            return refund.getId();
            
        } catch (StripeException e) {
            throw new Exception("Partial refund processing failed: " + e.getMessage());
        }
    }
    
    public Map<String, Object> createPaymentIntent(BigDecimal amount, String currency, String description) throws Exception {
        try {
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setDescription(description)
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            Map<String, Object> response = new HashMap<>();
            response.put("client_secret", paymentIntent.getClientSecret());
            response.put("payment_intent_id", paymentIntent.getId());
            response.put("amount", amountInCents);
            response.put("currency", currency);
            
            return response;
            
        } catch (StripeException e) {
            throw new Exception("Failed to create payment intent: " + e.getMessage());
        }
    }
    
    public boolean verifyPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            System.err.println("Failed to verify payment: " + e.getMessage());
            return false;
        }
    }
    
    public Map<String, Object> getPaymentStatus(String paymentIntentId) throws Exception {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("payment_intent_id", paymentIntent.getId());
            status.put("status", paymentIntent.getStatus());
            status.put("amount", paymentIntent.getAmount());
            status.put("currency", paymentIntent.getCurrency());
            status.put("created", paymentIntent.getCreated());
            
            return status;
            
        } catch (StripeException e) {
            throw new Exception("Failed to get payment status: " + e.getMessage());
        }
    }
    
    // Utility method to calculate platform fee
    public BigDecimal calculatePlatformFee(BigDecimal orderAmount) {
        // 3% platform fee
        return orderAmount.multiply(new BigDecimal("0.03"));
    }
    
    // Utility method to calculate restaurant commission
    public BigDecimal calculateRestaurantCommission(BigDecimal orderAmount) {
        // 15% commission to restaurant
        return orderAmount.multiply(new BigDecimal("0.15"));
    }
    
    // Utility method to calculate delivery partner commission
    public BigDecimal calculateDeliveryCommission(BigDecimal deliveryFee) {
        // 20% commission from delivery fee
        return deliveryFee.multiply(new BigDecimal("0.20"));
    }
}