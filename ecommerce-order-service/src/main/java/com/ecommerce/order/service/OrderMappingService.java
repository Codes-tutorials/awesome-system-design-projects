package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderMappingService {
    
    /**
     * Map Order entity to OrderResponse DTO
     */
    public OrderResponse mapToResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .paymentId(order.getPaymentId())
                .shippingAddressId(order.getShippingAddressId())
                .billingAddressId(order.getBillingAddressId())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .notes(order.getNotes())
                .source(order.getSource())
                .priority(order.getPriority())
                .isFlashSale(order.getIsFlashSale())
                .flashSaleId(order.getFlashSaleId())
                .items(mapOrderItemsToResponse(order.getItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .metadata(createMetadata(order))
                .build();
    }
    
    /**
     * Map list of Order entities to OrderResponse DTOs
     */
    public List<OrderResponse> mapToResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map OrderItem entities to OrderItemResponse DTOs
     */
    private List<OrderResponse.OrderItemResponse> mapOrderItemsToResponse(List<OrderItem> items) {
        if (items == null) {
            return null;
        }
        
        return items.stream()
                .map(this::mapOrderItemToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map single OrderItem to OrderItemResponse
     */
    private OrderResponse.OrderItemResponse mapOrderItemToResponse(OrderItem item) {
        if (item == null) {
            return null;
        }
        
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .sku(item.getSku())
                .productName(item.getProductName())
                .category(item.getCategory())
                .brand(item.getBrand())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .weight(item.getWeight())
                .dimensions(item.getDimensions())
                .isDigital(item.getIsDigital())
                .isGiftWrap(item.getIsGiftWrap())
                .giftMessage(item.getGiftMessage())
                .specialInstructions(item.getSpecialInstructions())
                .createdAt(item.getCreatedAt())
                .build();
    }
    
    /**
     * Create metadata map for order response
     */
    private Map<String, Object> createMetadata(Order order) {
        Map<String, Object> metadata = new HashMap<>();
        
        // Add calculated fields
        metadata.put("subtotal", order.calculateSubtotal());
        metadata.put("finalAmount", order.calculateFinalAmount());
        metadata.put("itemCount", order.getItems() != null ? order.getItems().size() : 0);
        metadata.put("totalQuantity", calculateTotalQuantity(order));
        
        // Add status information
        metadata.put("isModifiable", order.isModifiable());
        metadata.put("isCancellable", order.isCancellable());
        
        // Add timing information
        if (order.getCreatedAt() != null) {
            metadata.put("ageInMinutes", calculateAgeInMinutes(order));
        }
        
        // Add flash sale information
        if (Boolean.TRUE.equals(order.getIsFlashSale())) {
            metadata.put("flashSaleOrder", true);
            if (order.getFlashSaleId() != null) {
                metadata.put("flashSaleId", order.getFlashSaleId());
            }
        }
        
        // Add shipping information
        metadata.put("hasPhysicalItems", hasPhysicalItems(order));
        metadata.put("hasDigitalItems", hasDigitalItems(order));
        metadata.put("requiresShipping", requiresShipping(order));
        
        // Add payment information
        if (order.getPaymentId() != null) {
            metadata.put("hasPayment", true);
        }
        
        // Add version for optimistic locking
        metadata.put("version", order.getVersion());
        
        return metadata;
    }
    
    /**
     * Calculate total quantity of all items
     */
    private int calculateTotalQuantity(Order order) {
        if (order.getItems() == null) {
            return 0;
        }
        
        return order.getItems().stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
    
    /**
     * Calculate order age in minutes
     */
    private long calculateAgeInMinutes(Order order) {
        if (order.getCreatedAt() == null) {
            return 0;
        }
        
        return java.time.Duration.between(
                order.getCreatedAt(),
                java.time.LocalDateTime.now()
        ).toMinutes();
    }
    
    /**
     * Check if order has physical items
     */
    private boolean hasPhysicalItems(Order order) {
        if (order.getItems() == null) {
            return false;
        }
        
        return order.getItems().stream()
                .anyMatch(item -> !Boolean.TRUE.equals(item.getIsDigital()));
    }
    
    /**
     * Check if order has digital items
     */
    private boolean hasDigitalItems(Order order) {
        if (order.getItems() == null) {
            return false;
        }
        
        return order.getItems().stream()
                .anyMatch(item -> Boolean.TRUE.equals(item.getIsDigital()));
    }
    
    /**
     * Check if order requires shipping
     */
    private boolean requiresShipping(Order order) {
        return hasPhysicalItems(order);
    }
    
    /**
     * Map Order to event data for Kafka messages
     */
    public Map<String, Object> mapToEventData(Order order) {
        Map<String, Object> eventData = new HashMap<>();
        
        // Basic order information
        eventData.put("orderId", order.getOrderId());
        eventData.put("orderNumber", order.getOrderNumber());
        eventData.put("userId", order.getUserId());
        eventData.put("status", order.getStatus().toString());
        eventData.put("totalAmount", order.getTotalAmount());
        eventData.put("currency", order.getCurrency());
        eventData.put("source", order.getSource());
        eventData.put("priority", order.getPriority().toString());
        
        // Flash sale information
        if (Boolean.TRUE.equals(order.getIsFlashSale())) {
            eventData.put("isFlashSale", true);
            eventData.put("flashSaleId", order.getFlashSaleId());
        }
        
        // Payment information
        if (order.getPaymentId() != null) {
            eventData.put("paymentId", order.getPaymentId());
            eventData.put("paymentMethod", order.getPaymentMethod());
        }
        
        // Timestamps
        eventData.put("createdAt", order.getCreatedAt());
        eventData.put("updatedAt", order.getUpdatedAt());
        
        // Item summary
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            eventData.put("itemCount", order.getItems().size());
            eventData.put("totalQuantity", calculateTotalQuantity(order));
            
            // Product IDs for inventory tracking
            List<String> productIds = order.getItems().stream()
                    .map(OrderItem::getProductId)
                    .distinct()
                    .collect(Collectors.toList());
            eventData.put("productIds", productIds);
        }
        
        // Calculated amounts
        eventData.put("subtotal", order.calculateSubtotal());
        eventData.put("discountAmount", order.getDiscountAmount());
        eventData.put("taxAmount", order.getTaxAmount());
        eventData.put("shippingAmount", order.getShippingAmount());
        
        return eventData;
    }
    
    /**
     * Create summary response for list operations
     */
    public OrderResponse.OrderSummary mapToSummary(Order order) {
        if (order == null) {
            return null;
        }
        
        return OrderResponse.OrderSummary.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .itemCount(order.getItems() != null ? order.getItems().size() : 0)
                .createdAt(order.getCreatedAt())
                .isFlashSale(order.getIsFlashSale())
                .build();
    }
    
    /**
     * Map list of orders to summary list
     */
    public List<OrderResponse.OrderSummary> mapToSummaryList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        
        return orders.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }
}