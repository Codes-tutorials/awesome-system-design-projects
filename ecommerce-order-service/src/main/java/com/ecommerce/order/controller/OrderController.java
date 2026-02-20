package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.UpdateOrderRequest;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.service.RateLimitService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing e-commerce orders")
@CrossOrigin(origins = {"http://localhost:3000", "https://ecommerce.com"})
public class OrderController {
    
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;
    private final RateLimitService rateLimitService;
    
    @Autowired
    public OrderController(OrderService orderService, RateLimitService rateLimitService) {
        this.orderService = orderService;
        this.rateLimitService = rateLimitService;
    }
    
    /**
     * Create a new order
     */
    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Insufficient inventory"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @Timed(name = "order.create", description = "Time taken to create an order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        log.info("Creating order for user: {}", request.getUserId());
        
        // Apply rate limiting
        String rateLimitKey = "create-order:" + request.getUserId();
        rateLimitService.checkRateLimit(rateLimitKey, 10, 60); // 10 orders per minute per user
        
        try {
            CompletableFuture<OrderResponse> orderFuture = orderService.createOrderAsync(request);
            OrderResponse orderResponse = orderFuture.get(); // Wait for completion
            
            log.info("Order created successfully: {}", orderResponse.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
            
        } catch (Exception e) {
            log.error("Failed to create order for user: {}", request.getUserId(), e);
            throw e;
        }
    }
    
    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @Timed(name = "order.get", description = "Time taken to retrieve an order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.debug("Fetching order: {}", orderId);
        
        OrderResponse orderResponse = orderService.getOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * Update order
     */
    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Updates order details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update request")
    })
    @Timed(name = "order.update", description = "Time taken to update an order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        
        log.info("Updating order: {}", orderId);
        
        OrderResponse orderResponse = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * Cancel order
     */
    @DeleteMapping("/{orderId}")
    @Operation(summary = "Cancel order", description = "Cancels an existing order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
    })
    @Timed(name = "order.cancel", description = "Time taken to cancel an order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        
        log.info("Cancelling order: {} with reason: {}", orderId, reason);
        
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get user orders with pagination
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user orders", description = "Retrieves orders for a specific user with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @Timed(name = "order.getUserOrders", description = "Time taken to retrieve user orders")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @Parameter(description = "User ID") @PathVariable String userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        log.debug("Fetching orders for user: {}", userId);
        
        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * Confirm order (move from PENDING to CONFIRMED)
     */
    @PostMapping("/{orderId}/confirm")
    @Operation(summary = "Confirm order", description = "Confirms a pending order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be confirmed")
    })
    @Timed(name = "order.confirm", description = "Time taken to confirm an order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FULFILLMENT')")
    public ResponseEntity<OrderResponse> confirmOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.info("Confirming order: {}", orderId);
        
        OrderResponse orderResponse = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * Ship order
     */
    @PostMapping("/{orderId}/ship")
    @Operation(summary = "Ship order", description = "Marks order as shipped")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order shipped successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be shipped")
    })
    @Timed(name = "order.ship", description = "Time taken to ship an order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FULFILLMENT')")
    public ResponseEntity<OrderResponse> shipOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.info("Shipping order: {}", orderId);
        
        OrderResponse orderResponse = orderService.shipOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * Deliver order
     */
    @PostMapping("/{orderId}/deliver")
    @Operation(summary = "Deliver order", description = "Marks order as delivered")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order delivered successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "400", description = "Order cannot be delivered")
    })
    @Timed(name = "order.deliver", description = "Time taken to deliver an order")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FULFILLMENT')")
    public ResponseEntity<OrderResponse> deliverOrder(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.info("Delivering order: {}", orderId);
        
        OrderResponse orderResponse = orderService.deliverOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }
    
    /**
     * Get order status
     */
    @GetMapping("/{orderId}/status")
    @Operation(summary = "Get order status", description = "Retrieves current order status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order status retrieved"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @Timed(name = "order.getStatus", description = "Time taken to get order status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> getOrderStatus(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        
        log.debug("Getting status for order: {}", orderId);
        
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order.getStatus().toString());
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Service health check endpoint")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Order service is healthy");
    }
}