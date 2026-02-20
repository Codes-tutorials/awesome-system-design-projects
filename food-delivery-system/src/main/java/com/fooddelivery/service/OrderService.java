package com.fooddelivery.service;

import com.fooddelivery.model.*;
import com.fooddelivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for Order entity operations
 */
@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;
    
    @Autowired
    private DeliveryPartnerService deliveryPartnerService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private OrderQueueService orderQueueService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    @Autowired
    private LoadBalancingService loadBalancingService;
    
    @Autowired
    private CacheService cacheService;
    
    public Order createOrderFromCart(Long customerId, Long restaurantId, String deliveryAddress,
                                   Double deliveryLatitude, Double deliveryLongitude,
                                   PaymentMethod paymentMethod, String specialInstructions) {
        
        // Rate limiting check
        if (!rateLimitingService.isRestaurantOrderAllowed(restaurantId, customerId)) {
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        }
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        if (!restaurant.getIsActive() || !restaurant.getIsAcceptingOrders()) {
            throw new RuntimeException("Restaurant is not accepting orders");
        }
        
        // Check if restaurant is overloaded and suggest alternatives
        if (loadBalancingService.isRestaurantOverloaded(restaurantId)) {
            List<Long> alternatives = loadBalancingService.getAlternativeRestaurants(restaurantId);
            if (!alternatives.isEmpty()) {
                throw new RuntimeException("Restaurant is currently busy. Try restaurant ID: " + alternatives.get(0));
            }
        }
        
        // Check restaurant capacity
        if (!orderQueueService.canAcceptOrder(restaurantId, null)) {
            // Queue the order instead of rejecting
            return queueOrderForLater(customerId, restaurantId, deliveryAddress, 
                                    deliveryLatitude, deliveryLongitude, paymentMethod, specialInstructions);
        }
        
        // Detect burst traffic
        orderQueueService.detectBurstTraffic(restaurantId);
        
        // Get cart items for this restaurant
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndRestaurantId(customerId, restaurantId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("No items in cart for this restaurant");
        }
        
        // Create order
        Order order = new Order(customer, restaurant, deliveryAddress);
        order.setOrderNumber(generateOrderNumber());
        order.setDeliveryLatitude(deliveryLatitude);
        order.setDeliveryLongitude(deliveryLongitude);
        order.setPaymentMethod(paymentMethod);
        order.setSpecialInstructions(specialInstructions);
        
        // Calculate order totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            BigDecimal itemTotal = cartItem.getMenuItem().getPrice()
                    .multiply(new BigDecimal(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        order.setSubtotal(subtotal);
        order.setDeliveryFee(restaurant.getDeliveryFee());
        order.setTaxAmount(calculateTax(subtotal));
        order.calculateTotalAmount();
        
        // Check minimum order amount
        if (order.getSubtotal().compareTo(restaurant.getMinimumOrderAmount()) < 0) {
            throw new RuntimeException("Order amount is below minimum order amount of " + restaurant.getMinimumOrderAmount());
        }
        
        // Set estimated delivery time
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(restaurant.getEstimatedDeliveryTimeMinutes()));
        
        Order savedOrder = orderRepository.save(order);
        
        // Create order items from cart items
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getMenuItem().getPrice());
            orderItem.setSpecialInstructions(cartItem.getSpecialInstructions());
            orderItemRepository.save(orderItem);
        }
        
        // Create initial status history
        createStatusHistory(savedOrder, OrderStatus.PLACED, "Order placed by customer");
        
        // Update restaurant load
        orderQueueService.incrementRestaurantLoad(restaurantId);
        
        // Clear cart for this restaurant
        cartItemRepository.clearCartByUserIdAndRestaurantId(customerId, restaurantId);
        
        // Process payment if not cash on delivery
        if (paymentMethod != PaymentMethod.CASH_ON_DELIVERY) {
            try {
                String transactionId = paymentService.processPayment(savedOrder);
                savedOrder.setPaymentTransactionId(transactionId);
                savedOrder.setPaymentStatus(PaymentStatus.COMPLETED);
            } catch (Exception e) {
                savedOrder.setPaymentStatus(PaymentStatus.FAILED);
                savedOrder.setStatus(OrderStatus.CANCELLED);
                savedOrder.setCancelledReason("Payment failed: " + e.getMessage());
                savedOrder.setCancelledBy("SYSTEM");
                createStatusHistory(savedOrder, OrderStatus.CANCELLED, "Payment failed");
                
                // Decrement restaurant load since order failed
                orderQueueService.decrementRestaurantLoad(restaurantId);
            }
            orderRepository.save(savedOrder);
        }
        
        // Cache the order for quick access
        cacheService.cacheUser(customerId, savedOrder);
        
        // Send notifications
        notificationService.sendOrderConfirmation(savedOrder);
        notificationService.notifyRestaurantNewOrder(savedOrder);
        
        return savedOrder;
    }
    
    private Order queueOrderForLater(Long customerId, Long restaurantId, String deliveryAddress,
                                   Double deliveryLatitude, Double deliveryLongitude,
                                   PaymentMethod paymentMethod, String specialInstructions) {
        
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));
        
        // Create order in QUEUED status
        Order order = new Order(customer, restaurant, deliveryAddress);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.QUEUED);
        order.setDeliveryLatitude(deliveryLatitude);
        order.setDeliveryLongitude(deliveryLongitude);
        order.setPaymentMethod(paymentMethod);
        order.setSpecialInstructions(specialInstructions);
        
        // Calculate totals (same as regular order)
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndRestaurantId(customerId, restaurantId);
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            BigDecimal itemTotal = cartItem.getMenuItem().getPrice()
                    .multiply(new BigDecimal(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        order.setSubtotal(subtotal);
        order.setDeliveryFee(restaurant.getDeliveryFee());
        order.setTaxAmount(calculateTax(subtotal));
        order.calculateTotalAmount();
        
        Order savedOrder = orderRepository.save(order);
        
        // Determine priority based on customer type
        OrderPriority priority = determineOrderPriority(customer);
        
        // Queue the order
        orderQueueService.queueOrder(savedOrder, priority);
        
        // Create status history
        createStatusHistory(savedOrder, OrderStatus.QUEUED, "Order queued due to high demand");
        
        // Notify customer about queuing
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    private OrderPriority determineOrderPriority(User customer) {
        // Logic to determine priority based on customer tier, order history, etc.
        // For now, simple logic based on order count
        Long orderCount = orderRepository.countOrdersByCustomer(customer.getId());
        
        if (orderCount > 50) {
            return OrderPriority.HIGH; // VIP customer
        } else if (orderCount > 10) {
            return OrderPriority.MEDIUM; // Regular customer
        } else {
            return OrderPriority.LOW; // New customer
        }
    }
    
    public Order confirmOrder(Long orderId, Integer preparationTimeMinutes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Order cannot be confirmed in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.CONFIRMED);
        order.setPreparationTimeMinutes(preparationTimeMinutes);
        
        // Update estimated delivery time based on preparation time
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(preparationTimeMinutes + 30)); // 30 min delivery buffer
        
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.CONFIRMED, "Order confirmed by restaurant");
        
        // Notify customer
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Order startPreparation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be prepared in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.PREPARING);
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.PREPARING, "Order preparation started");
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Order markOrderReady(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("Order cannot be marked ready in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.PREPARED);
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.PREPARED, "Order ready for pickup");
        
        // Assign delivery partner
        try {
            DeliveryPartner deliveryPartner = deliveryPartnerService.assignDeliveryPartner(savedOrder);
            savedOrder.setDeliveryPartner(deliveryPartner);
            orderRepository.save(savedOrder);
            
            notificationService.notifyDeliveryPartnerNewOrder(savedOrder);
        } catch (Exception e) {
            System.err.println("Failed to assign delivery partner: " + e.getMessage());
        }
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Order pickupOrder(Long orderId, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PREPARED) {
            throw new RuntimeException("Order cannot be picked up in current status: " + order.getStatus());
        }
        
        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartnerId)) {
            throw new RuntimeException("Order is not assigned to this delivery partner");
        }
        
        order.updateStatus(OrderStatus.PICKED_UP);
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.PICKED_UP, "Order picked up by delivery partner");
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Order startDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.PICKED_UP) {
            throw new RuntimeException("Order cannot be out for delivery in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.OUT_FOR_DELIVERY);
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.OUT_FOR_DELIVERY, "Order out for delivery");
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Order deliverOrder(Long orderId, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Order cannot be delivered in current status: " + order.getStatus());
        }
        
        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(deliveryPartnerId)) {
            throw new RuntimeException("Order is not assigned to this delivery partner");
        }
        
        order.updateStatus(OrderStatus.DELIVERED);
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.DELIVERED, "Order delivered successfully");
        
        // Update delivery partner stats
        deliveryPartnerService.completeDelivery(deliveryPartnerId, savedOrder);
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        notificationService.requestOrderReview(savedOrder);
        
        return savedOrder;
    }
    
    public Order cancelOrder(Long orderId, String reason, String cancelledBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (!order.canBeCancelled()) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.CANCELLED);
        order.setCancelledReason(reason);
        order.setCancelledBy(cancelledBy);
        
        // Process refund if payment was made
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            try {
                paymentService.processRefund(order);
                order.setRefundStatus(RefundStatus.COMPLETED);
                order.setRefundAmount(order.getTotalAmount());
            } catch (Exception e) {
                order.setRefundStatus(RefundStatus.FAILED);
                System.err.println("Failed to process refund: " + e.getMessage());
            }
        }
        
        Order savedOrder = orderRepository.save(order);
        createStatusHistory(savedOrder, OrderStatus.CANCELLED, "Order cancelled: " + reason);
        
        notificationService.sendOrderStatusUpdate(savedOrder);
        
        return savedOrder;
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public Page<Order> findOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findOrdersByCustomer(customerId, pageable);
    }
    
    public Page<Order> findOrdersByRestaurant(Long restaurantId, Pageable pageable) {
        return orderRepository.findOrdersByRestaurant(restaurantId, pageable);
    }
    
    public Page<Order> findOrdersByDeliveryPartner(Long deliveryPartnerId, Pageable pageable) {
        return orderRepository.findOrdersByDeliveryPartner(deliveryPartnerId, pageable);
    }
    
    public List<Order> findActiveOrdersByRestaurant(Long restaurantId) {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.PREPARED
        );
        return orderRepository.findActiveOrdersByRestaurant(restaurantId, activeStatuses);
    }
    
    public List<Order> findDelayedOrders() {
        return orderRepository.findDelayedOrders(LocalDateTime.now());
    }
    
    public BigDecimal calculateRestaurantRevenue(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.calculateRestaurantRevenue(restaurantId, startDate, endDate);
    }
    
    public List<Object[]> getOrderStatusStatistics() {
        return orderRepository.getOrderStatusStatistics();
    }
    
    public List<Object[]> getDailyOrderCounts(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.getDailyOrderCounts(startDate);
    }
    
    private void createStatusHistory(Order order, OrderStatus status, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setNotes(notes);
        orderStatusHistoryRepository.save(history);
    }
    
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }
    
    private BigDecimal calculateTax(BigDecimal subtotal) {
        // 5% tax rate
        return subtotal.multiply(new BigDecimal("0.05"));
    }
}