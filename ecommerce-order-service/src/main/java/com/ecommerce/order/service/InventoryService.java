package com.ecommerce.order.service;

import com.ecommerce.order.exception.InsufficientInventoryException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class InventoryService {
    
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    
    private final WebClient webClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Random random = new Random();
    
    // Cache keys
    private static final String INVENTORY_CACHE_KEY = "inventory:";
    private static final String RESERVED_INVENTORY_KEY = "reserved:";
    
    @Autowired
    public InventoryService(WebClient.Builder webClientBuilder, RedisTemplate<String, Object> redisTemplate) {
        this.webClient = webClientBuilder
                .baseUrl("http://inventory-service:8081") // External inventory service URL
                .build();
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Check inventory availability with circuit breaker
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "checkAvailabilityFallback")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    public CompletableFuture<Boolean> checkAvailabilityAsync(String productId, Integer quantity) {
        log.debug("Checking inventory availability for product: {}, quantity: {}", productId, quantity);
        
        return webClient.get()
                .uri("/inventory/{productId}/availability?quantity={quantity}", productId, quantity)
                .retrieve()
                .bodyToMono(InventoryResponse.class)
                .map(response -> response.isAvailable() && response.getAvailableQuantity() >= quantity)
                .timeout(Duration.ofMillis(500))
                .toFuture();
    }
    
    /**
     * Synchronous version for backward compatibility
     */
    public boolean checkAvailability(String productId, Integer quantity) {
        try {
            return checkAvailabilityAsync(productId, quantity).get();
        } catch (Exception e) {
            log.error("Error checking inventory availability for product: {}", productId, e);
            return checkCachedAvailability(productId, quantity);
        }
    }
    
    /**
     * Fallback method for inventory availability check
     */
    public CompletableFuture<Boolean> checkAvailabilityFallback(String productId, Integer quantity, Exception ex) {
        log.warn("Inventory service unavailable, using cached data for product: {}", productId, ex);
        boolean available = checkCachedAvailability(productId, quantity);
        return CompletableFuture.completedFuture(available);
    }
    
    /**
     * Check cached inventory availability
     */
    public boolean checkCachedAvailability(String productId, Integer quantity) {
        try {
            String cacheKey = INVENTORY_CACHE_KEY + productId;
            Object cachedQuantity = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedQuantity instanceof Integer) {
                int available = (Integer) cachedQuantity;
                log.debug("Using cached inventory for product: {}, available: {}", productId, available);
                return available >= quantity;
            }
            
            // If no cached data, simulate availability (for demo purposes)
            return simulateInventoryAvailability(productId, quantity);
            
        } catch (Exception e) {
            log.error("Error checking cached inventory for product: {}", productId, e);
            return simulateInventoryAvailability(productId, quantity);
        }
    }
    
    /**
     * Reserve inventory for order
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "reserveInventoryFallback")
    @Retry(name = "inventory-service")
    public void reserveInventory(String productId, Integer quantity) {
        log.debug("Reserving inventory for product: {}, quantity: {}", productId, quantity);
        
        try {
            ReserveInventoryRequest request = new ReserveInventoryRequest(productId, quantity);
            
            InventoryResponse response = webClient.post()
                    .uri("/inventory/reserve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .timeout(Duration.ofSeconds(1))
                    .block();
            
            if (response == null || !response.isSuccess()) {
                throw new InsufficientInventoryException(
                    "Failed to reserve inventory for product: " + productId,
                    productId, quantity, response != null ? response.getAvailableQuantity() : 0
                );
            }
            
            // Cache the reservation
            cacheInventoryReservation(productId, quantity);
            
            log.debug("Successfully reserved inventory for product: {}, quantity: {}", productId, quantity);
            
        } catch (Exception e) {
            log.error("Failed to reserve inventory for product: {}", productId, e);
            throw new InsufficientInventoryException(
                "Inventory reservation failed for product: " + productId, productId, quantity, null
            );
        }
    }
    
    /**
     * Fallback method for inventory reservation
     */
    public void reserveInventoryFallback(String productId, Integer quantity, Exception ex) {
        log.warn("Inventory service unavailable for reservation, using fallback for product: {}", productId, ex);
        
        // In fallback, we'll allow the reservation but log it for manual reconciliation
        cacheInventoryReservation(productId, quantity);
        log.warn("Fallback inventory reservation for product: {}, quantity: {} - requires manual reconciliation", 
                productId, quantity);
    }
    
    /**
     * Release reserved inventory
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "releaseInventoryFallback")
    public void releaseReservedInventory(String productId, Integer quantity) {
        log.debug("Releasing reserved inventory for product: {}, quantity: {}", productId, quantity);
        
        try {
            ReleaseInventoryRequest request = new ReleaseInventoryRequest(productId, quantity);
            
            webClient.post()
                    .uri("/inventory/release")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(1))
                    .block();
            
            // Remove from cache
            removeInventoryReservation(productId, quantity);
            
            log.debug("Successfully released inventory for product: {}, quantity: {}", productId, quantity);
            
        } catch (Exception e) {
            log.error("Failed to release inventory for product: {}", productId, e);
            // Continue execution - this is not critical for order processing
        }
    }
    
    /**
     * Fallback method for inventory release
     */
    public void releaseInventoryFallback(String productId, Integer quantity, Exception ex) {
        log.warn("Inventory service unavailable for release, using fallback for product: {}", productId, ex);
        removeInventoryReservation(productId, quantity);
    }
    
    /**
     * Get current inventory level
     */
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "getInventoryLevelFallback")
    public Integer getInventoryLevel(String productId) {
        log.debug("Getting inventory level for product: {}", productId);
        
        try {
            InventoryResponse response = webClient.get()
                    .uri("/inventory/{productId}", productId)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .timeout(Duration.ofMillis(500))
                    .block();
            
            if (response != null) {
                // Cache the result
                cacheInventoryLevel(productId, response.getAvailableQuantity());
                return response.getAvailableQuantity();
            }
            
            return getCachedInventoryLevel(productId);
            
        } catch (Exception e) {
            log.error("Failed to get inventory level for product: {}", productId, e);
            return getCachedInventoryLevel(productId);
        }
    }
    
    /**
     * Fallback method for getting inventory level
     */
    public Integer getInventoryLevelFallback(String productId, Exception ex) {
        log.warn("Inventory service unavailable, using cached data for product: {}", productId, ex);
        return getCachedInventoryLevel(productId);
    }
    
    /**
     * Cache inventory level
     */
    private void cacheInventoryLevel(String productId, Integer quantity) {
        try {
            String cacheKey = INVENTORY_CACHE_KEY + productId;
            redisTemplate.opsForValue().set(cacheKey, quantity, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Failed to cache inventory level for product: {}", productId, e);
        }
    }
    
    /**
     * Get cached inventory level
     */
    private Integer getCachedInventoryLevel(String productId) {
        try {
            String cacheKey = INVENTORY_CACHE_KEY + productId;
            Object cachedQuantity = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedQuantity instanceof Integer) {
                return (Integer) cachedQuantity;
            }
            
            // Return simulated inventory if no cache
            return simulateInventoryLevel(productId);
            
        } catch (Exception e) {
            log.error("Failed to get cached inventory level for product: {}", productId, e);
            return simulateInventoryLevel(productId);
        }
    }
    
    /**
     * Cache inventory reservation
     */
    private void cacheInventoryReservation(String productId, Integer quantity) {
        try {
            String reservationKey = RESERVED_INVENTORY_KEY + productId;
            redisTemplate.opsForValue().increment(reservationKey, quantity);
            redisTemplate.expire(reservationKey, Duration.ofHours(1)); // Reservations expire after 1 hour
        } catch (Exception e) {
            log.error("Failed to cache inventory reservation for product: {}", productId, e);
        }
    }
    
    /**
     * Remove inventory reservation from cache
     */
    private void removeInventoryReservation(String productId, Integer quantity) {
        try {
            String reservationKey = RESERVED_INVENTORY_KEY + productId;
            redisTemplate.opsForValue().increment(reservationKey, -quantity);
        } catch (Exception e) {
            log.error("Failed to remove inventory reservation for product: {}", productId, e);
        }
    }
    
    /**
     * Simulate inventory availability (for demo/testing purposes)
     */
    private boolean simulateInventoryAvailability(String productId, Integer quantity) {
        // Simulate 90% availability rate
        boolean available = random.nextDouble() < 0.9;
        log.debug("Simulated inventory availability for product: {}, quantity: {}, available: {}", 
                 productId, quantity, available);
        return available;
    }
    
    /**
     * Simulate inventory level (for demo/testing purposes)
     */
    private Integer simulateInventoryLevel(String productId) {
        // Simulate random inventory between 0 and 1000
        int level = random.nextInt(1001);
        log.debug("Simulated inventory level for product: {}, level: {}", productId, level);
        return level;
    }
    
    /**
     * Bulk inventory check for multiple products
     */
    public Map<String, Boolean> checkBulkAvailability(Map<String, Integer> productQuantities) {
        Map<String, Boolean> results = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            try {
                boolean available = checkAvailability(productId, quantity);
                results.put(productId, available);
            } catch (Exception e) {
                log.error("Failed to check availability for product: {}", productId, e);
                results.put(productId, false);
            }
        }
        
        return results;
    }
    
    // DTOs for external service communication
    public static class InventoryResponse {
        private boolean available;
        private boolean success;
        private Integer availableQuantity;
        private String message;
        
        // Constructors
        public InventoryResponse() {}
        
        public InventoryResponse(boolean available, Integer availableQuantity) {
            this.available = available;
            this.availableQuantity = availableQuantity;
            this.success = true;
        }
        
        // Getters and Setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Integer getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class ReserveInventoryRequest {
        private String productId;
        private Integer quantity;
        private String reservationId;
        
        public ReserveInventoryRequest() {}
        
        public ReserveInventoryRequest(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
            this.reservationId = java.util.UUID.randomUUID().toString();
        }
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getReservationId() { return reservationId; }
        public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    }
    
    public static class ReleaseInventoryRequest {
        private String productId;
        private Integer quantity;
        private String reservationId;
        
        public ReleaseInventoryRequest() {}
        
        public ReleaseInventoryRequest(String productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getReservationId() { return reservationId; }
        public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    }
}