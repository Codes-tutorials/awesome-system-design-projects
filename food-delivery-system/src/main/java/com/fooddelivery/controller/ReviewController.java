package com.fooddelivery.controller;

import com.fooddelivery.model.Review;
import com.fooddelivery.service.ReviewService;
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
 * REST Controller for Review management operations
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Management", description = "APIs for rating and review operations")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    @Operation(summary = "Create review", description = "Submit a new review for restaurant, order, or delivery partner")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review,
                                             Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            review.getUser().setId(userId);
            Review savedReview = reviewService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Review restaurant", description = "Submit review for a restaurant")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Review> reviewRestaurant(@PathVariable Long restaurantId,
                                                 @RequestParam Integer rating,
                                                 @RequestParam(required = false) String comment,
                                                 @RequestParam(required = false) Long orderId,
                                                 Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Review review = reviewService.createRestaurantReview(userId, restaurantId, rating, comment, orderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/delivery-partner/{deliveryPartnerId}")
    @Operation(summary = "Review delivery partner", description = "Submit review for a delivery partner")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Review> reviewDeliveryPartner(@PathVariable Long deliveryPartnerId,
                                                      @RequestParam Integer rating,
                                                      @RequestParam(required = false) String comment,
                                                      @RequestParam(required = false) Long orderId,
                                                      Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Review review = reviewService.createDeliveryPartnerReview(userId, deliveryPartnerId, rating, comment, orderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/order/{orderId}")
    @Operation(summary = "Review order", description = "Submit comprehensive review for an order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> reviewOrder(@PathVariable Long orderId,
                                                          @RequestParam Integer overallRating,
                                                          @RequestParam(required = false) Integer restaurantRating,
                                                          @RequestParam(required = false) Integer deliveryRating,
                                                          @RequestParam(required = false) String comment,
                                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Map<String, Object> result = reviewService.createOrderReview(userId, orderId, overallRating, 
                    restaurantRating, deliveryRating, comment);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID", description = "Retrieve review details by ID")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.findById(id)
                .map(review -> ResponseEntity.ok(review))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get restaurant reviews", description = "Retrieve all reviews for a restaurant")
    public ResponseEntity<Page<Review>> getRestaurantReviews(@PathVariable Long restaurantId,
                                                           Pageable pageable) {
        Page<Review> reviews = reviewService.findRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/restaurant/{restaurantId}/summary")
    @Operation(summary = "Get restaurant review summary", description = "Get review statistics for a restaurant")
    public ResponseEntity<Map<String, Object>> getRestaurantReviewSummary(@PathVariable Long restaurantId) {
        Map<String, Object> summary = reviewService.getRestaurantReviewSummary(restaurantId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/delivery-partner/{deliveryPartnerId}")
    @Operation(summary = "Get delivery partner reviews", description = "Retrieve all reviews for a delivery partner")
    public ResponseEntity<Page<Review>> getDeliveryPartnerReviews(@PathVariable Long deliveryPartnerId,
                                                                Pageable pageable) {
        Page<Review> reviews = reviewService.findDeliveryPartnerReviews(deliveryPartnerId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/delivery-partner/{deliveryPartnerId}/summary")
    @Operation(summary = "Get delivery partner review summary", description = "Get review statistics for a delivery partner")
    public ResponseEntity<Map<String, Object>> getDeliveryPartnerReviewSummary(@PathVariable Long deliveryPartnerId) {
        Map<String, Object> summary = reviewService.getDeliveryPartnerReviewSummary(deliveryPartnerId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user reviews", description = "Retrieve all reviews submitted by a user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Page<Review>> getUserReviews(@PathVariable Long userId,
                                                     Pageable pageable) {
        Page<Review> reviews = reviewService.findUserReviews(userId, pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get order reviews", description = "Retrieve reviews for a specific order")
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.principal.id)")
    public ResponseEntity<List<Review>> getOrderReviews(@PathVariable Long orderId) {
        List<Review> reviews = reviewService.findOrderReviews(orderId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent reviews", description = "Retrieve recent reviews across the platform")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Review>> getRecentReviews(@RequestParam(defaultValue = "50") int limit) {
        List<Review> reviews = reviewService.findRecentReviews(limit);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated reviews", description = "Retrieve highest rated reviews")
    public ResponseEntity<List<Review>> getTopRatedReviews(@RequestParam(defaultValue = "20") int limit) {
        List<Review> reviews = reviewService.findTopRatedReviews(limit);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/rating/{rating}")
    @Operation(summary = "Get reviews by rating", description = "Retrieve reviews with specific rating")
    public ResponseEntity<Page<Review>> getReviewsByRating(@PathVariable Integer rating,
                                                         @RequestParam(required = false) Long restaurantId,
                                                         @RequestParam(required = false) Long deliveryPartnerId,
                                                         Pageable pageable) {
        Page<Review> reviews;
        if (restaurantId != null) {
            reviews = reviewService.findRestaurantReviewsByRating(restaurantId, rating, pageable);
        } else if (deliveryPartnerId != null) {
            reviews = reviewService.findDeliveryPartnerReviewsByRating(deliveryPartnerId, rating, pageable);
        } else {
            reviews = reviewService.findReviewsByRating(rating, pageable);
        }
        return ResponseEntity.ok(reviews);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update review", description = "Update an existing review")
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#id, authentication.principal.id)")
    public ResponseEntity<Review> updateReview(@PathVariable Long id,
                                             @Valid @RequestBody Review review) {
        try {
            review.setId(id);
            Review updatedReview = reviewService.updateReview(review);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/rating")
    @Operation(summary = "Update review rating", description = "Update the rating of an existing review")
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#id, authentication.principal.id)")
    public ResponseEntity<Review> updateReviewRating(@PathVariable Long id,
                                                   @RequestParam Integer rating) {
        try {
            Review review = reviewService.updateReviewRating(id, rating);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/comment")
    @Operation(summary = "Update review comment", description = "Update the comment of an existing review")
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#id, authentication.principal.id)")
    public ResponseEntity<Review> updateReviewComment(@PathVariable Long id,
                                                    @RequestParam String comment) {
        try {
            Review review = reviewService.updateReviewComment(id, comment);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/helpful")
    @Operation(summary = "Mark review as helpful", description = "Mark a review as helpful")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> markReviewHelpful(@PathVariable Long id,
                                                               Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            reviewService.markReviewHelpful(id, userId);
            return ResponseEntity.ok(Map.of("message", "Review marked as helpful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/report")
    @Operation(summary = "Report review", description = "Report inappropriate review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> reportReview(@PathVariable Long id,
                                                          @RequestParam String reason,
                                                          Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            reviewService.reportReview(id, userId, reason);
            return ResponseEntity.ok(Map.of("message", "Review reported successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/reported")
    @Operation(summary = "Get reported reviews", description = "Retrieve reviews that have been reported")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Review>> getReportedReviews(Pageable pageable) {
        Page<Review> reviews = reviewService.findReportedReviews(pageable);
        return ResponseEntity.ok(reviews);
    }
    
    @PutMapping("/{id}/moderate")
    @Operation(summary = "Moderate review", description = "Moderate a reported review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> moderateReview(@PathVariable Long id,
                                                            @RequestParam String action,
                                                            @RequestParam(required = false) String reason) {
        try {
            reviewService.moderateReview(id, action, reason);
            return ResponseEntity.ok(Map.of("message", "Review moderated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get review statistics", description = "Get overall review statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReviewStatistics() {
        Map<String, Object> statistics = reviewService.getReviewStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/analytics/trends")
    @Operation(summary = "Get review trends", description = "Get review trends over time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getReviewTrends(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trends = reviewService.getReviewTrends(days);
        return ResponseEntity.ok(trends);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete review", description = "Delete a review")
    @PreAuthorize("hasRole('ADMIN') or @reviewService.isReviewOwner(#id, authentication.principal.id)")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper method
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Extract user ID from authentication principal
        // Implementation depends on your authentication setup
        return 1L; // Placeholder
    }
}