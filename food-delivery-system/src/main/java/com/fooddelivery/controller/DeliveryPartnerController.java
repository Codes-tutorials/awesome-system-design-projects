package com.fooddelivery.controller;

import com.fooddelivery.model.DeliveryPartner;
import com.fooddelivery.service.DeliveryPartnerService;
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

import java.util.List;
import java.util.Map;

/**
 * REST Controller for DeliveryPartner management operations
 */
@RestController
@RequestMapping("/api/delivery-partners")
@Tag(name = "Delivery Partner Management", description = "APIs for delivery partner operations and management")
public class DeliveryPartnerController {
    
    @Autowired
    private DeliveryPartnerService deliveryPartnerService;
    
    @PostMapping
    @Operation(summary = "Register delivery partner", description = "Register a new delivery partner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartner> registerDeliveryPartner(@Valid @RequestBody DeliveryPartner deliveryPartner) {
        try {
            DeliveryPartner savedPartner = deliveryPartnerService.registerDeliveryPartner(deliveryPartner);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPartner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get delivery partner by ID", description = "Retrieve delivery partner details by ID")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeliveryPartner> getDeliveryPartnerById(@PathVariable Long id) {
        return deliveryPartnerService.findById(id)
                .map(partner -> ResponseEntity.ok(partner))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all delivery partners", description = "Retrieve all delivery partners with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DeliveryPartner>> getAllDeliveryPartners(Pageable pageable) {
        Page<DeliveryPartner> partners = deliveryPartnerService.findAll(pageable);
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active delivery partners", description = "Retrieve all active delivery partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryPartner>> getActiveDeliveryPartners() {
        List<DeliveryPartner> partners = deliveryPartnerService.findActivePartners();
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available delivery partners", description = "Retrieve delivery partners available for assignment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<DeliveryPartner>> getAvailableDeliveryPartners() {
        List<DeliveryPartner> partners = deliveryPartnerService.findAvailablePartners();
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby delivery partners", description = "Find delivery partners within specified radius")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<List<DeliveryPartner>> getNearbyDeliveryPartners(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        
        List<DeliveryPartner> partners = deliveryPartnerService.findNearbyPartners(latitude, longitude, radiusKm);
        return ResponseEntity.ok(partners);
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated delivery partners", description = "Retrieve delivery partners with highest ratings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryPartner>> getTopRatedDeliveryPartners(
            @RequestParam(defaultValue = "10") int limit) {
        List<DeliveryPartner> partners = deliveryPartnerService.findTopRatedPartners(limit);
        return ResponseEntity.ok(partners);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update delivery partner", description = "Update delivery partner information")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeliveryPartner> updateDeliveryPartner(@PathVariable Long id,
                                                               @Valid @RequestBody DeliveryPartner deliveryPartner) {
        try {
            deliveryPartner.setId(id);
            DeliveryPartner updatedPartner = deliveryPartnerService.updateDeliveryPartner(deliveryPartner);
            return ResponseEntity.ok(updatedPartner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/location")
    @Operation(summary = "Update location", description = "Update delivery partner's current location")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeliveryPartner> updateLocation(@PathVariable Long id,
                                                        @RequestParam Double latitude,
                                                        @RequestParam Double longitude) {
        try {
            DeliveryPartner partner = deliveryPartnerService.updateLocation(id, latitude, longitude);
            return ResponseEntity.ok(partner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/availability")
    @Operation(summary = "Update availability", description = "Update delivery partner availability status")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeliveryPartner> updateAvailability(@PathVariable Long id,
                                                            @RequestParam Boolean isAvailable) {
        try {
            DeliveryPartner partner = deliveryPartnerService.updateAvailability(id, isAvailable);
            return ResponseEntity.ok(partner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/status")
    @Operation(summary = "Update partner status", description = "Activate or deactivate delivery partner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryPartner> updatePartnerStatus(@PathVariable Long id,
                                                             @RequestParam Boolean isActive) {
        try {
            DeliveryPartner partner = deliveryPartnerService.updatePartnerStatus(id, isActive);
            return ResponseEntity.ok(partner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/vehicle")
    @Operation(summary = "Update vehicle information", description = "Update delivery partner's vehicle details")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<DeliveryPartner> updateVehicleInfo(@PathVariable Long id,
                                                           @RequestParam String vehicleType,
                                                           @RequestParam String vehicleNumber,
                                                           @RequestParam(required = false) String vehicleModel) {
        try {
            DeliveryPartner partner = deliveryPartnerService.updateVehicleInfo(id, vehicleType, vehicleNumber, vehicleModel);
            return ResponseEntity.ok(partner);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/rating")
    @Operation(summary = "Rate delivery partner", description = "Submit rating for delivery partner")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> rateDeliveryPartner(@PathVariable Long id,
                                                                 @RequestParam Integer rating,
                                                                 @RequestParam(required = false) String review) {
        try {
            deliveryPartnerService.addRating(id, rating, review);
            return ResponseEntity.ok(Map.of("message", "Rating submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get delivery partner statistics", description = "Retrieve delivery partner performance statistics")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getDeliveryPartnerStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = deliveryPartnerService.getPartnerStatistics(id);
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/earnings")
    @Operation(summary = "Get delivery partner earnings", description = "Calculate delivery partner earnings for specified period")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getDeliveryPartnerEarnings(@PathVariable Long id,
                                                                         @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> earnings = deliveryPartnerService.getPartnerEarnings(id, days);
            return ResponseEntity.ok(earnings);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/delivery-history")
    @Operation(summary = "Get delivery history", description = "Retrieve delivery partner's delivery history")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Page<Map<String, Object>>> getDeliveryHistory(@PathVariable Long id,
                                                                       Pageable pageable) {
        try {
            Page<Map<String, Object>> history = deliveryPartnerService.getDeliveryHistory(id, pageable);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/start-shift")
    @Operation(summary = "Start shift", description = "Mark delivery partner as starting their shift")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, String>> startShift(@PathVariable Long id) {
        try {
            deliveryPartnerService.startShift(id);
            return ResponseEntity.ok(Map.of("message", "Shift started successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/end-shift")
    @Operation(summary = "End shift", description = "Mark delivery partner as ending their shift")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, String>> endShift(@PathVariable Long id) {
        try {
            deliveryPartnerService.endShift(id);
            return ResponseEntity.ok(Map.of("message", "Shift ended successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/emergency")
    @Operation(summary = "Report emergency", description = "Report emergency situation during delivery")
    @PreAuthorize("hasRole('DELIVERY_PARTNER') and #id == authentication.principal.id")
    public ResponseEntity<Map<String, String>> reportEmergency(@PathVariable Long id,
                                                             @RequestParam String emergencyType,
                                                             @RequestParam String description,
                                                             @RequestParam(required = false) Double latitude,
                                                             @RequestParam(required = false) Double longitude) {
        try {
            deliveryPartnerService.reportEmergency(id, emergencyType, description, latitude, longitude);
            return ResponseEntity.ok(Map.of("message", "Emergency reported successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/performance-report")
    @Operation(summary = "Get performance report", description = "Generate performance report for all delivery partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getPerformanceReport(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> report = deliveryPartnerService.generatePerformanceReport(days);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/bulk-notification")
    @Operation(summary = "Send bulk notification", description = "Send notification to multiple delivery partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendBulkNotification(
            @RequestParam List<Long> partnerIds,
            @RequestParam String message,
            @RequestParam(defaultValue = "INFO") String priority) {
        try {
            deliveryPartnerService.sendBulkNotification(partnerIds, message, priority);
            return ResponseEntity.ok(Map.of("message", "Notifications sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete delivery partner", description = "Permanently delete delivery partner (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDeliveryPartner(@PathVariable Long id) {
        try {
            deliveryPartnerService.deleteDeliveryPartner(id);
            return ResponseEntity.ok(Map.of("message", "Delivery partner deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}