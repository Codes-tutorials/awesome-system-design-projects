package com.fooddelivery.controller;

import com.fooddelivery.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Analytics and Reporting operations
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics & Reporting", description = "APIs for system analytics, metrics, and reporting")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard metrics", description = "Retrieve key metrics for admin dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = analyticsService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/orders/statistics")
    @Operation(summary = "Get order statistics", description = "Retrieve comprehensive order statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Map<String, Object> statistics = analyticsService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/orders/trends")
    @Operation(summary = "Get order trends", description = "Retrieve order trends over time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getOrderTrends(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "daily") String granularity) {
        
        List<Map<String, Object>> trends = analyticsService.getOrderTrends(days, granularity);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/revenue/statistics")
    @Operation(summary = "Get revenue statistics", description = "Retrieve revenue statistics and breakdown")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Map<String, Object> statistics = analyticsService.getRevenueStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/revenue/trends")
    @Operation(summary = "Get revenue trends", description = "Retrieve revenue trends over time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRevenueTrends(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "daily") String granularity) {
        
        List<Map<String, Object>> trends = analyticsService.getRevenueTrends(days, granularity);
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/restaurants/performance")
    @Operation(summary = "Get restaurant performance", description = "Retrieve performance metrics for all restaurants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getRestaurantPerformance(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> performance = analyticsService.getRestaurantPerformance(days, limit);
        return ResponseEntity.ok(performance);
    }
    
    @GetMapping("/restaurants/{restaurantId}/analytics")
    @Operation(summary = "Get restaurant analytics", description = "Retrieve detailed analytics for specific restaurant")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANT_OWNER') and @restaurantService.isOwner(#restaurantId, authentication.principal.id))")
    public ResponseEntity<Map<String, Object>> getRestaurantAnalytics(@PathVariable Long restaurantId,
                                                                     @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analytics = analyticsService.getRestaurantAnalytics(restaurantId, days);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/delivery-partners/performance")
    @Operation(summary = "Get delivery partner performance", description = "Retrieve performance metrics for delivery partners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDeliveryPartnerPerformance(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Map<String, Object>> performance = analyticsService.getDeliveryPartnerPerformance(days, limit);
        return ResponseEntity.ok(performance);
    }
    
    @GetMapping("/delivery-partners/{partnerId}/analytics")
    @Operation(summary = "Get delivery partner analytics", description = "Retrieve detailed analytics for specific delivery partner")
    @PreAuthorize("hasRole('ADMIN') or #partnerId == authentication.principal.id")
    public ResponseEntity<Map<String, Object>> getDeliveryPartnerAnalytics(@PathVariable Long partnerId,
                                                                          @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analytics = analyticsService.getDeliveryPartnerAnalytics(partnerId, days);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/customers/behavior")
    @Operation(summary = "Get customer behavior analytics", description = "Retrieve customer behavior patterns and insights")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomerBehaviorAnalytics(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analytics = analyticsService.getCustomerBehaviorAnalytics(days);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/customers/segments")
    @Operation(summary = "Get customer segments", description = "Retrieve customer segmentation analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCustomerSegments() {
        List<Map<String, Object>> segments = analyticsService.getCustomerSegments();
        return ResponseEntity.ok(segments);
    }
    
    @GetMapping("/menu-items/popularity")
    @Operation(summary = "Get menu item popularity", description = "Retrieve most popular menu items across platform")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getMenuItemPopularity(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long restaurantId) {
        
        List<Map<String, Object>> popularity = analyticsService.getMenuItemPopularity(days, limit, restaurantId);
        return ResponseEntity.ok(popularity);
    }
    
    @GetMapping("/peak-hours")
    @Operation(summary = "Get peak hours analysis", description = "Retrieve peak ordering hours and patterns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPeakHoursAnalysis(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analysis = analyticsService.getPeakHoursAnalysis(days);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/geographic/distribution")
    @Operation(summary = "Get geographic distribution", description = "Retrieve geographic distribution of orders and customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getGeographicDistribution(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> distribution = analyticsService.getGeographicDistribution(days);
        return ResponseEntity.ok(distribution);
    }
    
    @GetMapping("/delivery/performance")
    @Operation(summary = "Get delivery performance metrics", description = "Retrieve delivery time and performance metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDeliveryPerformanceMetrics(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> metrics = analyticsService.getDeliveryPerformanceMetrics(days);
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/ratings/analysis")
    @Operation(summary = "Get ratings analysis", description = "Retrieve comprehensive ratings and review analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRatingsAnalysis(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analysis = analyticsService.getRatingsAnalysis(days);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/cancellations/analysis")
    @Operation(summary = "Get cancellation analysis", description = "Retrieve order cancellation patterns and reasons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCancellationAnalysis(
            @RequestParam(defaultValue = "30") int days) {
        
        Map<String, Object> analysis = analyticsService.getCancellationAnalysis(days);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/system/health")
    @Operation(summary = "Get system health metrics", description = "Retrieve system performance and health metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemHealthMetrics() {
        Map<String, Object> metrics = analyticsService.getSystemHealthMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/load/analysis")
    @Operation(summary = "Get load analysis", description = "Retrieve system load analysis and capacity metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLoadAnalysis(
            @RequestParam(defaultValue = "24") int hours) {
        
        Map<String, Object> analysis = analyticsService.getLoadAnalysis(hours);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/predictions/demand")
    @Operation(summary = "Get demand predictions", description = "Retrieve demand forecasting and predictions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDemandPredictions(
            @RequestParam(defaultValue = "7") int forecastDays) {
        
        Map<String, Object> predictions = analyticsService.getDemandPredictions(forecastDays);
        return ResponseEntity.ok(predictions);
    }
    
    @GetMapping("/financial/summary")
    @Operation(summary = "Get financial summary", description = "Retrieve comprehensive financial summary and KPIs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFinancialSummary(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Map<String, Object> summary = analyticsService.getFinancialSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/export/report")
    @Operation(summary = "Export analytics report", description = "Generate and export comprehensive analytics report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> exportAnalyticsReport(
            @RequestParam String reportType,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "PDF") String format) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Map<String, Object> report = analyticsService.generateReport(reportType, startDate, endDate, format);
        return ResponseEntity.ok(report);
    }
    
    @PostMapping("/alerts/configure")
    @Operation(summary = "Configure analytics alerts", description = "Set up automated alerts for key metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> configureAnalyticsAlerts(
            @RequestBody Map<String, Object> alertConfig) {
        try {
            analyticsService.configureAlerts(alertConfig);
            return ResponseEntity.ok(Map.of("message", "Analytics alerts configured successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/alerts/status")
    @Operation(summary = "Get alerts status", description = "Retrieve current status of analytics alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAlertsStatus() {
        List<Map<String, Object>> alerts = analyticsService.getAlertsStatus();
        return ResponseEntity.ok(alerts);
    }
}