package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.FlashSaleStats;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.FlashSaleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flash-sales")
@Tag(name = "Flash Sale Management", description = "APIs for managing flash sale orders")
@CrossOrigin(origins = {"http://localhost:3000", "https://ecommerce.com"})
public class FlashSaleController {
    
    private static final Logger log = LoggerFactory.getLogger(FlashSaleController.class);
    
    private final FlashSaleService flashSaleService;
    private final RateLimitService rateLimitService;
    
    @Autowired
    public FlashSaleController(FlashSaleService flashSaleService, RateLimitService rateLimitService) {
        this.flashSaleService = flashSaleService;
        this.rateLimitService = rateLimitService;
    }
    
    /**
     * Create flash sale order
     */
    @PostMapping("/{flashSaleId}/orders")
    @Operation(summary = "Create flash sale order", description = "Creates an order during a flash sale event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Order queued for processing"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "409", description = "Flash sale not active or sold out")
    })
    @Timed(name = "flashsale.order.create", description = "Time taken to create a flash sale order")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> createFlashSaleOrder(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId,
            @Valid @RequestBody CreateOrderRequest request) {
        
        log.info("Creating flash sale order for user: {} in sale: {}", request.getUserId(), flashSaleId);
        
        // Apply aggressive rate limiting for flash sales
        String globalRateLimitKey = "flash-sale-global:" + flashSaleId;
        String userRateLimitKey = "flash-sale-user:" + flashSaleId + ":" + request.getUserId();
        
        // Global rate limit: 10,000 orders per minute for the flash sale
        rateLimitService.checkRateLimit(globalRateLimitKey, 10000, 60);
        
        // Per-user rate limit: 1 order per minute per user for flash sale
        rateLimitService.checkRateLimit(userRateLimitKey, 1, 60);
        
        // Set flash sale properties
        request.setIsFlashSale(true);
        request.setFlashSaleId(flashSaleId);
        
        try {
            OrderResponse orderResponse = flashSaleService.createFlashSaleOrder(flashSaleId, request);
            
            log.info("Flash sale order queued successfully: {}", orderResponse.getOrderId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderResponse);
            
        } catch (Exception e) {
            log.error("Failed to create flash sale order for user: {} in sale: {}", 
                     request.getUserId(), flashSaleId, e);
            throw e;
        }
    }
    
    /**
     * Get flash sale statistics
     */
    @GetMapping("/{flashSaleId}/stats")
    @Operation(summary = "Get flash sale statistics", description = "Retrieves statistics for a flash sale event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Flash sale not found")
    })
    @Timed(name = "flashsale.stats.get", description = "Time taken to get flash sale statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALYTICS')")
    public ResponseEntity<FlashSaleStats> getFlashSaleStats(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId) {
        
        log.debug("Getting statistics for flash sale: {}", flashSaleId);
        
        FlashSaleStats stats = flashSaleService.getFlashSaleStats(flashSaleId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get flash sale queue status
     */
    @GetMapping("/{flashSaleId}/queue-status")
    @Operation(summary = "Get flash sale queue status", description = "Retrieves current queue status for flash sale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Queue status retrieved"),
        @ApiResponse(responseCode = "404", description = "Flash sale not found")
    })
    @Timed(name = "flashsale.queue.status", description = "Time taken to get queue status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getFlashSaleQueueStatus(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId) {
        
        log.debug("Getting queue status for flash sale: {}", flashSaleId);
        
        Object queueStatus = flashSaleService.getQueueStatus(flashSaleId);
        return ResponseEntity.ok(queueStatus);
    }
    
    /**
     * Start flash sale processing
     */
    @PostMapping("/{flashSaleId}/start")
    @Operation(summary = "Start flash sale", description = "Starts processing for a flash sale event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Flash sale started successfully"),
        @ApiResponse(responseCode = "400", description = "Flash sale cannot be started")
    })
    @Timed(name = "flashsale.start", description = "Time taken to start flash sale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> startFlashSale(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId) {
        
        log.info("Starting flash sale: {}", flashSaleId);
        
        flashSaleService.startFlashSale(flashSaleId);
        return ResponseEntity.ok("Flash sale started successfully");
    }
    
    /**
     * Stop flash sale processing
     */
    @PostMapping("/{flashSaleId}/stop")
    @Operation(summary = "Stop flash sale", description = "Stops processing for a flash sale event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Flash sale stopped successfully"),
        @ApiResponse(responseCode = "400", description = "Flash sale cannot be stopped")
    })
    @Timed(name = "flashsale.stop", description = "Time taken to stop flash sale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> stopFlashSale(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId) {
        
        log.info("Stopping flash sale: {}", flashSaleId);
        
        flashSaleService.stopFlashSale(flashSaleId);
        return ResponseEntity.ok("Flash sale stopped successfully");
    }
    
    /**
     * Get user's position in flash sale queue
     */
    @GetMapping("/{flashSaleId}/queue-position/{userId}")
    @Operation(summary = "Get user queue position", description = "Gets user's position in flash sale queue")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Queue position retrieved"),
        @ApiResponse(responseCode = "404", description = "User not in queue")
    })
    @Timed(name = "flashsale.queue.position", description = "Time taken to get queue position")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Object> getUserQueuePosition(
            @Parameter(description = "Flash Sale ID") @PathVariable String flashSaleId,
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        log.debug("Getting queue position for user: {} in flash sale: {}", userId, flashSaleId);
        
        Object position = flashSaleService.getUserQueuePosition(flashSaleId, userId);
        return ResponseEntity.ok(position);
    }
    
    /**
     * Health check for flash sale service
     */
    @GetMapping("/health")
    @Operation(summary = "Flash sale service health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Flash sale service is healthy");
    }
}