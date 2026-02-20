package com.ecommerce.order.service;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.event.OrderEvent;
import com.ecommerce.order.event.OrderEventType;
import com.ecommerce.order.exception.InsufficientInventoryException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.PaymentFailedException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderCacheService orderCacheService;
    private final OrderValidationService validationService;
    private final OrderMappingService mappingService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository,
                       InventoryService inventoryService,
                       PaymentService paymentService,
                       NotificationService notificationService,
                       KafkaTemplate<String, Object> kafkaTemplate,
                       OrderCacheService orderCacheService,
                       OrderValidationService validationService,
                       OrderMappingService mappingService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.kafkaTemplate = kafkaTemplate;
        this.orderCacheService = orderCacheService;
        this.validationService = validationService;
        this.mappingService = mappingService;
    }
    
    /**
     * Create a new order asynchronously
     */
    @Async("orderProcessingExecutor")
    public CompletableFuture<OrderResponse> createOrderAsync(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());
        
        try {
            // 1. Validate request
            validationService.validateCreateOrderRequest(request);
            
            // 2. Check inventory availability
            checkInventoryAvailability(request.getItems());
            
            // 3. Reserve inventory
            reserveInventory(request.getItems());
            
            // 4. Create order entity
            Order order = createOrderEntity(request);
            
            // 5. Save order to database
            Order savedOrder = orderRepository.save(order);
            log.info("Order saved with ID: {}", savedOrder.getOrderId());
            
            // 6. Process payment asynchronously
            processPaymentAsync(savedOrder);
            
            // 7. Publish order created event
            publishOrderEvent(savedOrder, OrderEventType.ORDER_CREATED);
            
            // 8. Cache order for quick access
            orderCacheService.cacheOrder(savedOrder);
            
            // 9. Send confirmation notification
            notificationService.sendOrderConfirmation(savedOrder);
            
            return CompletableFuture.completedFuture(mappingService.mapToResponse(savedOrder));
            
        } catch (Exception e) {
            log.error("Failed to create order for user: {}", request.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * Get order by ID with caching
     */
    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponse getOrder(String orderId) {
        log.debug("Fetching order: {}", orderId);
        
        Order order = orderRepository.findByOrderIdWithItems(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        return mappingService.mapToResponse(order);
    }
    
    /**
     * Update order
     */
    @CachePut(value = "orders", key = "#orderId")
    public OrderResponse updateOrder(String orderId, UpdateOrderRequest request) {
        log.info("Updating order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        // Validate update request
        validationService.validateUpdateOrderRequest(order, request);
        
        // Apply updates
        applyOrderUpdates(order, request);
        
        // Save updated order
        Order updatedOrder = orderRepository.save(order);
        
        // Publish update event
        publishOrderEvent(updatedOrder, OrderEventType.ORDER_UPDATED);
        
        return mappingService.mapToResponse(updatedOrder);
    }
    
    /**
     * Cancel order
     */
    @CacheEvict(value = "orders", key = "#orderId")
    public void cancelOrder(String orderId, String reason) {
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        // Validate cancellation
        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        // Cancel order
        order.markAsCancelled(reason);
        orderRepository.save(order);
        
        // Release reserved inventory
        releaseInventoryAsync(order);
        
        // Process refund if payment was made
        processRefundAsync(order);
        
        // Publish cancellation event
        publishOrderEvent(order, OrderEventType.ORDER_CANCELLED);
        
        // Send cancellation notification
        notificationService.sendOrderCancellation(order, reason);
    }
    
    /**
     * Get user orders with pagination
     */
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(mappingService::mapToResponse);
    }
    
    /**
     * Confirm order (move from PENDING to CONFIRMED)
     */
    public OrderResponse confirmOrder(String orderId) {
        log.info("Confirming order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be confirmed in current status: " + order.getStatus());
        }
        
        order.markAsConfirmed();
        Order confirmedOrder = orderRepository.save(order);
        
        // Update cache
        orderCacheService.updateOrderCache(confirmedOrder);
        
        // Publish confirmation event
        publishOrderEvent(confirmedOrder, OrderEventType.ORDER_CONFIRMED);
        
        // Send confirmation notification
        notificationService.sendOrderConfirmation(confirmedOrder);
        
        return mappingService.mapToResponse(confirmedOrder);
    }
    
    /**
     * Ship order
     */
    public OrderResponse shipOrder(String orderId) {
        log.info("Shipping order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalStateException("Order cannot be shipped in current status: " + order.getStatus());
        }
        
        order.markAsShipped();
        Order shippedOrder = orderRepository.save(order);
        
        // Update cache
        orderCacheService.updateOrderCache(shippedOrder);
        
        // Publish shipping event
        publishOrderEvent(shippedOrder, OrderEventType.ORDER_SHIPPED);
        
        // Send shipping notification
        notificationService.sendOrderShipped(shippedOrder);
        
        return mappingService.mapToResponse(shippedOrder);
    }
    
    /**
     * Deliver order
     */
    public OrderResponse deliverOrder(String orderId) {
        log.info("Delivering order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Order cannot be delivered in current status: " + order.getStatus());
        }
        
        order.markAsDelivered();
        Order deliveredOrder = orderRepository.save(order);
        
        // Update cache
        orderCacheService.updateOrderCache(deliveredOrder);
        
        // Publish delivery event
        publishOrderEvent(deliveredOrder, OrderEventType.ORDER_DELIVERED);
        
        // Send delivery notification
        notificationService.sendOrderDelivered(deliveredOrder);
        
        return mappingService.mapToResponse(deliveredOrder);
    }
    
    /**
     * Check inventory availability with circuit breaker
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "fallbackInventoryCheck")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    private void checkInventoryAvailability(List<CreateOrderRequest.OrderItemRequest> items) {
        log.debug("Checking inventory availability for {} items", items.size());
        
        for (CreateOrderRequest.OrderItemRequest item : items) {
            boolean available = inventoryService.checkAvailability(item.getProductId(), item.getQuantity());
            if (!available) {
                throw new InsufficientInventoryException(
                    "Insufficient inventory for product: " + item.getProductId());
            }
        }
    }
    
    /**
     * Fallback method for inventory check
     */
    private void fallbackInventoryCheck(List<CreateOrderRequest.OrderItemRequest> items, Exception ex) {
        log.warn("Inventory service unavailable, using cached data", ex);
        
        // Use cached inventory data or allow order with warning
        for (CreateOrderRequest.OrderItemRequest item : items) {
            boolean available = inventoryService.checkCachedAvailability(item.getProductId(), item.getQuantity());
            if (!available) {
                throw new InsufficientInventoryException(
                    "Insufficient inventory for product (cached): " + item.getProductId());
            }
        }
    }
    
    /**
     * Reserve inventory for order items
     */
    private void reserveInventory(List<CreateOrderRequest.OrderItemRequest> items) {
        log.debug("Reserving inventory for {} items", items.size());
        
        for (CreateOrderRequest.OrderItemRequest item : items) {
            inventoryService.reserveInventory(item.getProductId(), item.getQuantity());
        }
    }
    
    /**
     * Release reserved inventory asynchronously
     */
    @Async("orderProcessingExecutor")
    private void releaseInventoryAsync(Order order) {
        log.debug("Releasing inventory for order: {}", order.getOrderId());
        
        for (OrderItem item : order.getItems()) {
            inventoryService.releaseReservedInventory(item.getProductId(), item.getQuantity());
        }
    }
    
    /**
     * Process payment asynchronously
     */
    @Async("paymentProcessingExecutor")
    private void processPaymentAsync(Order order) {
        log.debug("Processing payment for order: {}", order.getOrderId());
        
        try {
            String paymentId = paymentService.processPayment(order);
            
            // Update order with payment information
            order.setPaymentId(paymentId);
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            // Update cache
            orderCacheService.updateOrderCache(order);
            
            // Publish payment success event
            publishOrderEvent(order, OrderEventType.PAYMENT_COMPLETED);
            
        } catch (PaymentFailedException e) {
            log.error("Payment failed for order: {}", order.getOrderId(), e);
            
            // Update order status
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            
            // Release inventory
            releaseInventoryAsync(order);
            
            // Publish payment failure event
            publishOrderEvent(order, OrderEventType.PAYMENT_FAILED);
        }
    }
    
    /**
     * Process refund asynchronously
     */
    @Async("paymentProcessingExecutor")
    private void processRefundAsync(Order order) {
        if (order.getPaymentId() != null) {
            log.debug("Processing refund for order: {}", order.getOrderId());
            
            try {
                paymentService.processRefund(order.getPaymentId(), order.getTotalAmount());
            } catch (Exception e) {
                log.error("Refund failed for order: {}", order.getOrderId(), e);
                // Handle refund failure - might need manual intervention
            }
        }
    }
    
    /**
     * Create order entity from request
     */
    private Order createOrderEntity(CreateOrderRequest request) {
        Order order = new Order(request.getUserId(), request.calculateTotalAmount());
        
        // Set basic properties
        order.setCurrency(request.getCurrency());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setShippingAddressId(request.getShippingAddressId());
        order.setBillingAddressId(request.getBillingAddressId());
        order.setDiscountAmount(request.getDiscountAmount());
        order.setTaxAmount(request.getTaxAmount());
        order.setShippingAmount(request.getShippingAmount());
        order.setNotes(request.getNotes());
        order.setSource(request.getSource());
        order.setIsFlashSale(request.getIsFlashSale());
        order.setFlashSaleId(request.getFlashSaleId());
        
        // Create order items
        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> createOrderItem(itemRequest, order))
            .collect(Collectors.toList());
        
        order.setItems(items);
        
        return order;
    }
    
    /**
     * Create order item from request
     */
    private OrderItem createOrderItem(CreateOrderRequest.OrderItemRequest itemRequest, Order order) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(itemRequest.getProductId());
        item.setSku(itemRequest.getSku());
        item.setProductName(itemRequest.getProductName());
        item.setCategory(itemRequest.getCategory());
        item.setBrand(itemRequest.getBrand());
        item.setQuantity(itemRequest.getQuantity());
        item.setUnitPrice(itemRequest.getUnitPrice());
        item.setTotalPrice(itemRequest.getTotalPrice());
        item.setWeight(itemRequest.getWeight());
        item.setDimensions(itemRequest.getDimensions());
        item.setIsDigital(itemRequest.getIsDigital());
        item.setIsGiftWrap(itemRequest.getIsGiftWrap());
        item.setGiftMessage(itemRequest.getGiftMessage());
        item.setSpecialInstructions(itemRequest.getSpecialInstructions());
        
        return item;
    }
    
    /**
     * Apply updates to order
     */
    private void applyOrderUpdates(Order order, UpdateOrderRequest request) {
        if (request.getShippingAddressId() != null) {
            order.setShippingAddressId(request.getShippingAddressId());
        }
        if (request.getBillingAddressId() != null) {
            order.setBillingAddressId(request.getBillingAddressId());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        // Add other updateable fields as needed
    }
    
    /**
     * Publish order event to Kafka
     */
    private void publishOrderEvent(Order order, OrderEventType eventType) {
        try {
            OrderEvent event = OrderEvent.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .eventType(eventType)
                .orderData(mappingService.mapToEventData(order))
                .timestamp(java.time.Instant.now())
                .build();
            
            String topic = determineTopicByEventType(eventType);
            String partitionKey = order.getUserId(); // Partition by user for ordering
            
            kafkaTemplate.send(topic, partitionKey, event)
                .addCallback(
                    result -> log.debug("Event published successfully: {}", event.getOrderId()),
                    failure -> log.error("Failed to publish event for order: {}", order.getOrderId(), failure)
                );
                
        } catch (Exception e) {
            log.error("Failed to publish order event", e);
            // Don't fail the main operation due to event publishing failure
        }
    }
    
    /**
     * Determine Kafka topic based on event type
     */
    private String determineTopicByEventType(OrderEventType eventType) {
        return switch (eventType) {
            case ORDER_CREATED -> "order.created";
            case ORDER_UPDATED -> "order.updated";
            case ORDER_CONFIRMED -> "order.confirmed";
            case ORDER_SHIPPED -> "order.shipped";
            case ORDER_DELIVERED -> "order.delivered";
            case ORDER_CANCELLED -> "order.cancelled";
            case PAYMENT_COMPLETED -> "order.payment.completed";
            case PAYMENT_FAILED -> "order.payment.failed";
            default -> "order.events";
        };
    }
}