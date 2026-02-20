package com.fooddelivery.controller;

import com.fooddelivery.dto.CreateOrderRequest;
import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.model.Order;
import com.fooddelivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order management operations
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for order creation, tracking, and management")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    @Operation(summary = "Create new order", description = "Create order from cart items")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                   Authentication authentication) {
        try {
            Long customerId = getUserIdFromAuthentication(authentication);
            Order order = orderService.createOrderFromCart(
                    customerId,
                    request.getRestaurantId(),
                    request.getDeliveryAddress(),
                    request.getDeliveryLatitude(),
                    request.getDeliveryLongitude(),
                    request.getPaymentMethod(),
                    request.getSpecialInstructions()
            );
            
            OrderResponse response = convertToOrderResponse(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER') or hasRole('DELIVERY_PARTNER') or @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(order -> ResponseEntity.ok(convertToOrderResponse(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieve order details by order number")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER') or hasRole('DELIVERY_PARTNER') or @orderService.isOrderOwnerByNumber(#orderNumber, authentication.principal.id)")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(order -> ResponseEntity.ok(convertToOrderResponse(order)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer orders", description = "Retrieve all orders for a specific customer")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(@PathVariable Long customerId,
                                                               Pageable pageable) {
        Page<Order> orders = orderService.findOrdersByCustomer(customerId, pageable);
        Page<OrderResponse> orderResponses = orders.map(this::convertToOrderResponse);
        return ResponseEntity.ok(orderResponses);
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant orders", description = "Retrieve all orders for a specific restaurant")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#restaurantId, authentication.principal.id))")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrders(@PathVariable Long restaurantId,
                                                                 Pageable pageable) {
        Page<Order> orders = orderService.findOrdersByRestaurant(restaurantId, pageable);
        Page<OrderResponse> orderResponses = orders.map(this::convertToOrderResponse);
        return ResponseEntity.ok(orderResponses);
    }
    
    @GetMapping("/restaurant/{restaurantId}/active")
    @Operation(summary = "Get active restaurant orders", description = "Retrieve active orders for a restaurant")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#restaurantId, authentication.principal.id))")
    public ResponseEntity<List<OrderResponse>> getActiveRestaurantOrders(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.findActiveOrdersByRestaurant(restaurantId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToOrderResponse)
                .toList();
        return ResponseEntity.ok(orderResponses);
    }
    
    @GetMapping("/delivery-partner/{deliveryPartnerId}")
    @Operation(summary = "Get delivery partner orders", description = "Retrieve orders assigned to delivery partner")
    @PreAuthorize("hasRole('ADMIN') or #deliveryPartnerId == authentication.principal.id")
    public ResponseEntity<Page<OrderResponse>> getDeliveryPartnerOrders(@PathVariable Long deliveryPartnerId,
                                                                       Pageable pageable) {
        Page<Order> orders = orderService.findOrdersByDeliveryPartner(deliveryPartnerId, pageable);
        Page<OrderResponse> orderResponses = orders.map(this::convertToOrderResponse);
        return ResponseEntity.ok(orderResponses);
    }
    
    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm order", description = "Restaurant confirms the order")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @orderService.isRestaurantOrder(#id, authentication.principal.restaurantId))")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id,
                                                    @RequestParam Integer preparationTimeMinutes) {
        try {
            Order order = orderService.confirmOrder(id, preparationTimeMinutes);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/start-preparation")
    @Operation(summary = "Start order preparation", description = "Mark order as being prepared")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @orderService.isRestaurantOrder(#id, authentication.principal.restaurantId))")
    public ResponseEntity<OrderResponse> startPreparation(@PathVariable Long id) {
        try {
            Order order = orderService.startPreparation(id);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/ready")
    @Operation(summary = "Mark order ready", description = "Mark order as ready for pickup")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @orderService.isRestaurantOrder(#id, authentication.principal.restaurantId))")
    public ResponseEntity<OrderResponse> markOrderReady(@PathVariable Long id) {
        try {
            Order order = orderService.markOrderReady(id);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/pickup")
    @Operation(summary = "Pickup order", description = "Delivery partner picks up the order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderResponse> pickupOrder(@PathVariable Long id,
                                                   Authentication authentication) {
        try {
            Long deliveryPartnerId = getUserIdFromAuthentication(authentication);
            Order order = orderService.pickupOrder(id, deliveryPartnerId);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/start-delivery")
    @Operation(summary = "Start delivery", description = "Mark order as out for delivery")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('DELIVERY_PARTNER') and @orderService.isDeliveryPartnerOrder(#id, authentication.principal.id))")
    public ResponseEntity<OrderResponse> startDelivery(@PathVariable Long id) {
        try {
            Order order = orderService.startDelivery(id);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/deliver")
    @Operation(summary = "Deliver order", description = "Mark order as delivered")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DELIVERY_PARTNER')")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long id,
                                                    Authentication authentication) {
        try {
            Long deliveryPartnerId = getUserIdFromAuthentication(authentication);
            Order order = orderService.deliverOrder(id, deliveryPartnerId);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order with reason")
    @PreAuthorize("hasRole('ADMIN') or @orderService.canCancelOrder(#id, authentication.principal.id, authentication.authorities)")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                   @RequestParam String reason,
                                                   Authentication authentication) {
        try {
            String cancelledBy = getUserRoleFromAuthentication(authentication);
            Order order = orderService.cancelOrder(id, reason, cancelledBy);
            return ResponseEntity.ok(convertToOrderResponse(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/rating")
    @Operation(summary = "Rate order", description = "Customer rates the order and delivery")
    @PreAuthorize("hasRole('CUSTOMER') and @orderService.isOrderOwner(#id, authentication.principal.id)")
    public ResponseEntity<Map<String, String>> rateOrder(@PathVariable Long id,
                                                        @RequestParam Integer customerRating,
                                                        @RequestParam(required = false) String customerFeedback,
                                                        @RequestParam(required = false) Integer restaurantRating,
                                                        @RequestParam(required = false) Integer deliveryRating) {
        try {
            orderService.rateOrder(id, customerRating, customerFeedback, restaurantRating, deliveryRating);
            return ResponseEntity.ok(Map.of("message", "Order rated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/delayed")
    @Operation(summary = "Get delayed orders", description = "Retrieve orders that are delayed beyond estimated time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getDelayedOrders() {
        List<Order> orders = orderService.findDelayedOrders();
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::convertToOrderResponse)
                .toList();
        return ResponseEntity.ok(orderResponses);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get order statistics", description = "Retrieve order status statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getOrderStatistics() {
        List<Object[]> statistics = orderService.getOrderStatusStatistics();
        List<Map<String, Object>> result = statistics.stream()
                .map(stat -> Map.of(
                        "status", stat[0],
                        "count", stat[1]
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/daily-counts")
    @Operation(summary = "Get daily order counts", description = "Retrieve daily order counts for specified period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDailyOrderCounts(
            @RequestParam(defaultValue = "30") int days) {
        List<Object[]> counts = orderService.getDailyOrderCounts(days);
        List<Map<String, Object>> result = counts.stream()
                .map(count -> Map.of(
                        "date", count[0],
                        "orderCount", count[1]
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/restaurant/{restaurantId}/revenue")
    @Operation(summary = "Get restaurant revenue", description = "Calculate restaurant revenue for specified period")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#restaurantId, authentication.principal.id))")
    public ResponseEntity<Map<String, Object>> getRestaurantRevenue(@PathVariable Long restaurantId,
                                                                   @RequestParam(required = false) LocalDateTime startDate,
                                                                   @RequestParam(required = false) LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        BigDecimal revenue = orderService.calculateRestaurantRevenue(restaurantId, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "restaurantId", restaurantId,
                "startDate", startDate,
                "endDate", endDate,
                "revenue", revenue
        ));
    }
    
    // Helper methods
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Extract user ID from authentication principal
        // Implementation depends on your authentication setup
        return 1L; // Placeholder
    }
    
    private String getUserRoleFromAuthentication(Authentication authentication) {
        // Extract user role from authentication
        // Implementation depends on your authentication setup
        return "CUSTOMER"; // Placeholder
    }
    
    private OrderResponse convertToOrderResponse(Order order) {
        // Convert Order entity to OrderResponse DTO
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setSubtotal(order.getSubtotal());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTaxAmount(order.getTaxAmount());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setActualDeliveryTime(order.getActualDeliveryTime());
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setCreatedAt(order.getCreatedAt());
        
        if (order.getCustomer() != null) {
            response.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        }
        
        if (order.getRestaurant() != null) {
            response.setRestaurantName(order.getRestaurant().getName());
        }
        
        if (order.getDeliveryPartner() != null) {
            response.setDeliveryPartnerName(order.getDeliveryPartner().getFirstName() + " " + order.getDeliveryPartner().getLastName());
        }
        
        return response;
    }
}