package com.rideshare.controller;

import com.rideshare.dto.*;
import com.rideshare.model.Trip;
import com.rideshare.model.User;
import com.rideshare.service.TripService;
import com.rideshare.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST Controller for trip-related operations
 */
@RestController
@RequestMapping("/api/trips")
@Tag(name = "Trip Management", description = "APIs for managing ride requests and trips")
public class TripController {
    
    private static final Logger logger = LoggerFactory.getLogger(TripController.class);
    
    @Autowired
    private TripService tripService;
    
    @Autowired
    private PricingService pricingService;
    
    /**
     * Request a new trip
     */
    @PostMapping("/request")
    @Operation(summary = "Request a new trip", description = "Create a new trip request and find available drivers")
    public ResponseEntity<TripResponse> requestTrip(
            @Valid @RequestBody TripRequestDto request,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip request received from user: {}", user.getId());
        
        try {
            Trip trip = tripService.requestTrip(request, user);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing trip request for user: {}", user.getId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get fare estimate for a trip
     */
    @PostMapping("/estimate-fare")
    @Operation(summary = "Get fare estimate", description = "Calculate estimated fare for a trip without booking")
    public ResponseEntity<FareEstimateResponse> estimateFare(
            @Valid @RequestBody FareEstimateRequest request) {
        
        logger.debug("Fare estimate request: {} to {}", request.getPickupAddress(), request.getDestinationAddress());
        
        try {
            FareEstimateResponse estimate = tripService.estimateFare(request);
            return ResponseEntity.ok(estimate);
            
        } catch (Exception e) {
            logger.error("Error calculating fare estimate", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get current trip for user
     */
    @GetMapping("/current")
    @Operation(summary = "Get current active trip", description = "Get the current active trip for the authenticated user")
    public ResponseEntity<TripResponse> getCurrentTrip(@AuthenticationPrincipal User user) {
        
        try {
            Trip currentTrip = tripService.getCurrentTrip(user);
            
            if (currentTrip != null) {
                TripResponse response = TripResponse.fromTrip(currentTrip);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.noContent().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting current trip for user: {}", user.getId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get trip details by ID
     */
    @GetMapping("/{tripId}")
    @Operation(summary = "Get trip details", description = "Get detailed information about a specific trip")
    public ResponseEntity<TripResponse> getTripDetails(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @AuthenticationPrincipal User user) {
        
        try {
            Trip trip = tripService.getTripDetails(tripId, user);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting trip details for trip: {}", tripId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Cancel a trip
     */
    @PostMapping("/{tripId}/cancel")
    @Operation(summary = "Cancel trip", description = "Cancel an active trip")
    public ResponseEntity<TripResponse> cancelTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @Valid @RequestBody TripCancellationRequest request,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip cancellation request for trip: {} by user: {}", tripId, user.getId());
        
        try {
            Trip trip = tripService.cancelTrip(tripId, user, request.getReason());
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cancelling trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Rate a completed trip
     */
    @PostMapping("/{tripId}/rate")
    @Operation(summary = "Rate trip", description = "Rate a completed trip and provide feedback")
    public ResponseEntity<Void> rateTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @Valid @RequestBody TripRatingRequest request,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip rating request for trip: {} by user: {}", tripId, user.getId());
        
        try {
            tripService.rateTrip(tripId, user, request.getRating(), request.getFeedback());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error rating trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get trip history for user
     */
    @GetMapping("/history")
    @Operation(summary = "Get trip history", description = "Get paginated trip history for the authenticated user")
    public ResponseEntity<Page<TripHistoryResponse>> getTripHistory(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        
        try {
            Page<TripHistoryResponse> history = tripService.getTripHistory(user, pageable);
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            logger.error("Error getting trip history for user: {}", user.getId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get surge pricing information for an area
     */
    @GetMapping("/surge-info")
    @Operation(summary = "Get surge pricing info", description = "Get current surge pricing information for a location")
    public ResponseEntity<SurgeInfoResponse> getSurgeInfo(
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude) {
        
        try {
            PricingService.SurgeInfo surgeInfo = pricingService.getSurgeInfo(latitude, longitude);
            SurgeInfoResponse response = new SurgeInfoResponse(
                surgeInfo.getMultiplier(),
                surgeInfo.getLevel()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting surge info for location: {}, {}", latitude, longitude, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Driver-specific endpoints
    
    /**
     * Accept a trip (Driver)
     */
    @PostMapping("/{tripId}/accept")
    @Operation(summary = "Accept trip (Driver)", description = "Accept a trip request as a driver")
    public ResponseEntity<TripResponse> acceptTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip acceptance request for trip: {} by driver: {}", tripId, user.getId());
        
        try {
            Trip trip = tripService.acceptTrip(tripId, user);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error accepting trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reject a trip (Driver)
     */
    @PostMapping("/{tripId}/reject")
    @Operation(summary = "Reject trip (Driver)", description = "Reject a trip request as a driver")
    public ResponseEntity<Void> rejectTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @Valid @RequestBody TripRejectionRequest request,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip rejection request for trip: {} by driver: {}", tripId, user.getId());
        
        try {
            tripService.rejectTrip(tripId, user, request.getReason());
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error rejecting trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Mark driver as arrived (Driver)
     */
    @PostMapping("/{tripId}/arrived")
    @Operation(summary = "Mark driver arrived", description = "Mark that driver has arrived at pickup location")
    public ResponseEntity<TripResponse> markDriverArrived(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @AuthenticationPrincipal User user) {
        
        logger.info("Driver arrived notification for trip: {} by driver: {}", tripId, user.getId());
        
        try {
            Trip trip = tripService.markDriverArrived(tripId, user);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error marking driver arrived for trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Start trip (Driver)
     */
    @PostMapping("/{tripId}/start")
    @Operation(summary = "Start trip (Driver)", description = "Start the trip after picking up the rider")
    public ResponseEntity<TripResponse> startTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip start request for trip: {} by driver: {}", tripId, user.getId());
        
        try {
            Trip trip = tripService.startTrip(tripId, user);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error starting trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Complete trip (Driver)
     */
    @PostMapping("/{tripId}/complete")
    @Operation(summary = "Complete trip (Driver)", description = "Complete the trip after reaching destination")
    public ResponseEntity<TripResponse> completeTrip(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @Valid @RequestBody TripCompletionRequest request,
            @AuthenticationPrincipal User user) {
        
        logger.info("Trip completion request for trip: {} by driver: {}", tripId, user.getId());
        
        try {
            Trip trip = tripService.completeTrip(tripId, user, request);
            TripResponse response = TripResponse.fromTrip(trip);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error completing trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update trip location (Driver)
     */
    @PostMapping("/{tripId}/location")
    @Operation(summary = "Update trip location", description = "Update driver's current location during trip")
    public ResponseEntity<Void> updateTripLocation(
            @Parameter(description = "Trip ID") @PathVariable String tripId,
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal User user) {
        
        try {
            tripService.updateTripLocation(tripId, user, request);
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Error updating trip location for trip: {}", tripId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}