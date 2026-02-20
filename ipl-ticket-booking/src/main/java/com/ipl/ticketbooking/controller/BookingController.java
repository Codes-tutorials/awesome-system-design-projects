package com.ipl.ticketbooking.controller;

import com.ipl.ticketbooking.dto.BookingRequest;
import com.ipl.ticketbooking.dto.BookingResponse;
import com.ipl.ticketbooking.service.BookingService;
import com.ipl.ticketbooking.service.RateLimitingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * BOOKING CONTROLLER: Handles high-concurrency ticket booking requests
 * Implements rate limiting and request validation for burst traffic handling
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Management", description = "APIs for ticket booking operations")
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    /**
     * CRITICAL ENDPOINT: Books tickets with concurrency control
     * Handles millions of concurrent requests during IPL ticket sales
     */
    @PostMapping
    @Operation(summary = "Book tickets for a match", 
               description = "Books selected seats for a user with distributed locking and rate limiting")
    public ResponseEntity<?> bookTickets(@Valid @RequestBody BookingRequest request,
                                        HttpServletRequest httpRequest) {
        
        String clientIP = getClientIP(httpRequest);
        long startTime = System.currentTimeMillis();
        
        try {
            // Multi-layer rate limiting for burst protection
            if (!rateLimitingService.isAllowed(request.getUserId(), "booking")) {
                logger.warn("User rate limit exceeded for booking: userId={}", request.getUserId());
                return ResponseEntity.status(429).body(createErrorResponse("RATE_LIMIT_EXCEEDED", 
                    "Too many booking requests. Please wait before trying again."));
            }
            
            if (!rateLimitingService.isAllowedByIP(clientIP, "booking")) {
                logger.warn("IP rate limit exceeded for booking: ip={}", clientIP);
                return ResponseEntity.status(429).body(createErrorResponse("IP_RATE_LIMIT_EXCEEDED", 
                    "Too many requests from your network. Please try again later."));
            }
            
            if (!rateLimitingService.isGloballyAllowed("booking")) {
                logger.warn("Global rate limit exceeded for booking");
                return ResponseEntity.status(503).body(createErrorResponse("SYSTEM_BUSY", 
                    "System is experiencing high traffic. Please try again in a few moments."));
            }
            
            // Process booking
            BookingResponse response = bookingService.bookSeats(request);
            
            long processingTime = System.currentTimeMillis() - startTime;
            logger.info("Booking request processed successfully in {}ms for user {} and match {}", 
                       processingTime, request.getUserId(), request.getMatchId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("Booking request failed in {}ms for user {} and match {}: {}", 
                        processingTime, request.getUserId(), request.getMatchId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Cancels a booking
     */
    @PostMapping("/{bookingId}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId,
                                          @RequestParam Long userId,
                                          @RequestParam(required = false) String reason,
                                          HttpServletRequest httpRequest) {
        
        String clientIP = getClientIP(httpRequest);
        
        try {
            // Rate limiting for cancellation requests
            if (!rateLimitingService.isAllowed(userId, "booking")) {
                return ResponseEntity.status(429).body(createErrorResponse("RATE_LIMIT_EXCEEDED", 
                    "Too many requests. Please wait before trying again."));
            }
            
            bookingService.cancelBooking(bookingId, userId, reason != null ? reason : "User requested");
            
            logger.info("Booking {} cancelled by user {} from IP {}", bookingId, userId, clientIP);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Booking cancelled successfully",
                "bookingId", bookingId
            ));
            
        } catch (Exception e) {
            logger.error("Booking cancellation failed for booking {} by user {}: {}", 
                        bookingId, userId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets booking details
     */
    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details")
    public ResponseEntity<?> getBooking(@PathVariable Long bookingId,
                                       @RequestParam Long userId) {
        
        try {
            // Rate limiting for read operations
            if (!rateLimitingService.isAllowed(userId, "search")) {
                return ResponseEntity.status(429).body(createErrorResponse("RATE_LIMIT_EXCEEDED", 
                    "Too many requests. Please wait before trying again."));
            }
            
            // Implementation would fetch booking details
            // For now, return placeholder
            return ResponseEntity.ok().body(Map.of(
                "message", "Booking details endpoint - implementation pending",
                "bookingId", bookingId
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch booking {} for user {}: {}", bookingId, userId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets user's booking history
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's booking history")
    public ResponseEntity<?> getUserBookings(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // Rate limiting for read operations
            if (!rateLimitingService.isAllowed(userId, "search")) {
                return ResponseEntity.status(429).body(createErrorResponse("RATE_LIMIT_EXCEEDED", 
                    "Too many requests. Please wait before trying again."));
            }
            
            // Implementation would fetch user's bookings with pagination
            return ResponseEntity.ok().body(Map.of(
                "message", "User bookings endpoint - implementation pending",
                "userId", userId,
                "page", page,
                "size", size
            ));
            
        } catch (Exception e) {
            logger.error("Failed to fetch bookings for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Health check endpoint for load balancers
     */
    @GetMapping("/health")
    @Operation(summary = "Health check for booking service")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok().body(Map.of(
            "status", "healthy",
            "service", "booking-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    // Utility methods
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        return Map.of(
            "errorCode", errorCode,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
    }
}