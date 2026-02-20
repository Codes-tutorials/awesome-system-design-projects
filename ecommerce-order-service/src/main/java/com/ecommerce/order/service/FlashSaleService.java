package com.ecommerce.order.service;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.FlashSaleStats;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.event.FlashSaleEvent;
import com.ecommerce.order.exception.FlashSaleNotActiveException;
import com.ecommerce.order.exception.FlashSaleSoldOutException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FlashSaleService {
    
    private static final Logger log = LoggerFactory.getLogger(FlashSaleService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;
    private final RateLimitService rateLimitService;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    
    // Redis key patterns
    private static final String FLASH_SALE_KEY = "flash-sale:";
    private static final String FLASH_SALE_QUEUE_KEY = "flash-sale-queue:";
    private static final String FLASH_SALE_STATS_KEY = "flash-sale-stats:";
    private static final String FLASH_SALE_ACTIVE_KEY = "flash-sale-active:";
    private static final String FLASH_SALE_INVENTORY_KEY = "flash-sale-inventory:";
    
    @Autowired
    public FlashSaleService(RedisTemplate<String, Object> redisTemplate,
                           OrderService orderService,
                           RateLimitService rateLimitService,
                           OrderRepository orderRepository,
                           InventoryService inventoryService) {
        this.redisTemplate = redisTemplate;
        this.orderService = orderService;
        this.rateLimitService = rateLimitService;
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }
    
    /**
     * Create flash sale order
     */
    public OrderResponse createFlashSaleOrder(String flashSaleId, CreateOrderRequest request) {
        log.info("Processing flash sale order for user: {} in sale: {}", request.getUserId(), flashSaleId);
        
        // 1. Validate flash sale is active
        validateFlashSale(flashSaleId);
        
        // 2. Check inventory availability for flash sale
        checkFlashSaleInventory(flashSaleId, request);
        
        // 3. Apply user-specific rate limiting
        rateLimitService.checkUserRateLimit(request.getUserId(), "flash-sale");
        
        // 4. Queue order for processing
        String queuePosition = queueFlashSaleOrder(flashSaleId, request);
        
        // 5. Create pending order response
        OrderResponse pendingResponse = createPendingOrderResponse(request, queuePosition);
        
        // 6. Update flash sale statistics
        updateFlashSaleStats(flashSaleId, "queued");
        
        log.info("Flash sale order queued at position: {} for user: {}", queuePosition, request.getUserId());
        return pendingResponse;
    }
    
    /**
     * Get flash sale statistics
     */
    public FlashSaleStats getFlashSaleStats(String flashSaleId) {
        log.debug("Getting statistics for flash sale: {}", flashSaleId);
        
        String statsKey = FLASH_SALE_STATS_KEY + flashSaleId;
        Map<Object, Object> statsMap = redisTemplate.opsForHash().entries(statsKey);
        
        if (statsMap.isEmpty()) {
            // Generate stats from database if not cached
            return generateFlashSaleStats(flashSaleId);
        }
        
        return FlashSaleStats.builder()
            .flashSaleId(flashSaleId)
            .totalOrders(getLongValue(statsMap, "totalOrders"))
            .successfulOrders(getLongValue(statsMap, "successfulOrders"))
            .failedOrders(getLongValue(statsMap, "failedOrders"))
            .queuedOrders(getLongValue(statsMap, "queuedOrders"))
            .totalRevenue(getDoubleValue(statsMap, "totalRevenue"))
            .averageOrderValue(getDoubleValue(statsMap, "averageOrderValue"))
            .peakOrdersPerSecond(getLongValue(statsMap, "peakOrdersPerSecond"))
            .currentQueueSize(getQueueSize(flashSaleId))
            .isActive(isFlashSaleActive(flashSaleId))
            .build();
    }
    
    /**
     * Get queue status for flash sale
     */
    public Object getQueueStatus(String flashSaleId) {
        String queueKey = FLASH_SALE_QUEUE_KEY + flashSaleId;
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        
        Map<String, Object> status = new HashMap<>();
        status.put("flashSaleId", flashSaleId);
        status.put("queueSize", queueSize);
        status.put("isActive", isFlashSaleActive(flashSaleId));
        status.put("processingRate", getProcessingRate(flashSaleId));
        status.put("estimatedWaitTime", calculateEstimatedWaitTime(queueSize));
        
        return status;
    }
    
    /**
     * Start flash sale processing
     */
    public void startFlashSale(String flashSaleId) {
        log.info("Starting flash sale: {}", flashSaleId);
        
        String activeKey = FLASH_SALE_ACTIVE_KEY + flashSaleId;
        redisTemplate.opsForValue().set(activeKey, true, Duration.ofHours(24));
        
        // Initialize flash sale statistics
        initializeFlashSaleStats(flashSaleId);
        
        // Start processing queue
        startQueueProcessing(flashSaleId);
        
        log.info("Flash sale started successfully: {}", flashSaleId);
    }
    
    /**
     * Stop flash sale processing
     */
    public void stopFlashSale(String flashSaleId) {
        log.info("Stopping flash sale: {}", flashSaleId);
        
        String activeKey = FLASH_SALE_ACTIVE_KEY + flashSaleId;
        redisTemplate.delete(activeKey);
        
        // Process remaining queue items
        processRemainingQueue(flashSaleId);
        
        log.info("Flash sale stopped successfully: {}", flashSaleId);
    }
    
    /**
     * Get user's position in queue
     */
    public Object getUserQueuePosition(String flashSaleId, String userId) {
        String queueKey = FLASH_SALE_QUEUE_KEY + flashSaleId;
        
        // This is a simplified implementation - in production, you'd need a more efficient way
        // to track user positions, possibly using Redis sorted sets
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        
        Map<String, Object> position = new HashMap<>();
        position.put("flashSaleId", flashSaleId);
        position.put("userId", userId);
        position.put("estimatedPosition", queueSize); // Simplified
        position.put("estimatedWaitTime", calculateEstimatedWaitTime(queueSize));
        
        return position;
    }
    
    /**
     * Validate flash sale is active and available
     */
    private void validateFlashSale(String flashSaleId) {
        if (!isFlashSaleActive(flashSaleId)) {
            throw new FlashSaleNotActiveException("Flash sale is not active: " + flashSaleId);
        }
        
        // Check if flash sale has inventory available
        if (!hasFlashSaleInventory(flashSaleId)) {
            throw new FlashSaleSoldOutException("Flash sale is sold out: " + flashSaleId);
        }
    }
    
    /**
     * Check if flash sale is active
     */
    private boolean isFlashSaleActive(String flashSaleId) {
        String activeKey = FLASH_SALE_ACTIVE_KEY + flashSaleId;
        Boolean isActive = (Boolean) redisTemplate.opsForValue().get(activeKey);
        return Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Check flash sale inventory availability
     */
    private void checkFlashSaleInventory(String flashSaleId, CreateOrderRequest request) {
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            String inventoryKey = FLASH_SALE_INVENTORY_KEY + flashSaleId + ":" + item.getProductId();
            Long availableQuantity = (Long) redisTemplate.opsForValue().get(inventoryKey);
            
            if (availableQuantity == null || availableQuantity < item.getQuantity()) {
                throw new FlashSaleSoldOutException(
                    "Insufficient flash sale inventory for product: " + item.getProductId());
            }
        }
    }
    
    /**
     * Check if flash sale has inventory
     */
    private boolean hasFlashSaleInventory(String flashSaleId) {
        String pattern = FLASH_SALE_INVENTORY_KEY + flashSaleId + ":*";
        return !redisTemplate.keys(pattern).isEmpty();
    }
    
    /**
     * Queue flash sale order for processing
     */
    private String queueFlashSaleOrder(String flashSaleId, CreateOrderRequest request) {
        String queueKey = FLASH_SALE_QUEUE_KEY + flashSaleId;
        
        // Add timestamp and unique ID for tracking
        Map<String, Object> queueItem = new HashMap<>();
        queueItem.put("request", request);
        queueItem.put("timestamp", System.currentTimeMillis());
        queueItem.put("queueId", UUID.randomUUID().toString());
        
        // Add to queue (left push for FIFO processing with right pop)
        Long position = redisTemplate.opsForList().leftPush(queueKey, queueItem);
        
        // Set TTL for queue to prevent memory leaks
        redisTemplate.expire(queueKey, Duration.ofHours(24));
        
        return String.valueOf(position);
    }
    
    /**
     * Create pending order response
     */
    private OrderResponse createPendingOrderResponse(CreateOrderRequest request, String queuePosition) {
        return OrderResponse.builder()
            .orderId(UUID.randomUUID().toString()) // Temporary ID
            .orderNumber("PENDING-" + System.currentTimeMillis())
            .userId(request.getUserId())
            .status(OrderStatus.PENDING)
            .totalAmount(request.calculateTotalAmount())
            .currency(request.getCurrency())
            .createdAt(LocalDateTime.now())
            .metadata(Map.of(
                "queuePosition", queuePosition,
                "isFlashSale", true,
                "flashSaleId", request.getFlashSaleId()
            ))
            .build();
    }
    
    /**
     * Process flash sale queue asynchronously
     */
    @EventListener
    @Async("flashSaleExecutor")
    public void processFlashSaleQueue(FlashSaleEvent event) {
        String flashSaleId = event.getFlashSaleId();
        String queueKey = FLASH_SALE_QUEUE_KEY + flashSaleId;
        
        log.info("Starting flash sale queue processing for: {}", flashSaleId);
        
        while (isFlashSaleActive(flashSaleId)) {
            try {
                // Pop item from queue (right pop for FIFO)
                Object queueItem = redisTemplate.opsForList().rightPop(queueKey, Duration.ofSeconds(1));
                
                if (queueItem == null) {
                    continue; // No items in queue, continue polling
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> item = (Map<String, Object>) queueItem;
                CreateOrderRequest request = (CreateOrderRequest) item.get("request");
                
                processFlashSaleOrderItem(flashSaleId, request);
                
            } catch (Exception e) {
                log.error("Error processing flash sale queue item for sale: {}", flashSaleId, e);
                // Continue processing other items
            }
        }
        
        log.info("Flash sale queue processing completed for: {}", flashSaleId);
    }
    
    /**
     * Process individual flash sale order item
     */
    private void processFlashSaleOrderItem(String flashSaleId, CreateOrderRequest request) {
        try {
            // Reserve flash sale inventory
            reserveFlashSaleInventory(flashSaleId, request);
            
            // Create order through regular order service
            orderService.createOrderAsync(request).get(30, TimeUnit.SECONDS);
            
            // Update success statistics
            updateFlashSaleStats(flashSaleId, "success");
            
            log.debug("Flash sale order processed successfully for user: {}", request.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to process flash sale order for user: {}", request.getUserId(), e);
            
            // Update failure statistics
            updateFlashSaleStats(flashSaleId, "failure");
            
            // Handle failure - might need to notify user
            handleFlashSaleOrderFailure(flashSaleId, request, e);
        }
    }
    
    /**
     * Reserve flash sale inventory
     */
    private void reserveFlashSaleInventory(String flashSaleId, CreateOrderRequest request) {
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            String inventoryKey = FLASH_SALE_INVENTORY_KEY + flashSaleId + ":" + item.getProductId();
            
            Long remaining = redisTemplate.opsForValue().decrement(inventoryKey, item.getQuantity());
            
            if (remaining < 0) {
                // Rollback and throw exception
                redisTemplate.opsForValue().increment(inventoryKey, item.getQuantity());
                throw new FlashSaleSoldOutException(
                    "Flash sale inventory exhausted for product: " + item.getProductId());
            }
        }
    }
    
    /**
     * Handle flash sale order failure
     */
    private void handleFlashSaleOrderFailure(String flashSaleId, CreateOrderRequest request, Exception e) {
        // Release reserved flash sale inventory
        releaseFlashSaleInventory(flashSaleId, request);
        
        // Could implement retry logic or user notification here
        log.warn("Flash sale order failed for user: {} in sale: {}", request.getUserId(), flashSaleId);
    }
    
    /**
     * Release flash sale inventory
     */
    private void releaseFlashSaleInventory(String flashSaleId, CreateOrderRequest request) {
        for (CreateOrderRequest.OrderItemRequest item : request.getItems()) {
            String inventoryKey = FLASH_SALE_INVENTORY_KEY + flashSaleId + ":" + item.getProductId();
            redisTemplate.opsForValue().increment(inventoryKey, item.getQuantity());
        }
    }
    
    /**
     * Update flash sale statistics
     */
    private void updateFlashSaleStats(String flashSaleId, String operation) {
        String statsKey = FLASH_SALE_STATS_KEY + flashSaleId;
        
        switch (operation) {
            case "queued" -> redisTemplate.opsForHash().increment(statsKey, "queuedOrders", 1);
            case "success" -> {
                redisTemplate.opsForHash().increment(statsKey, "successfulOrders", 1);
                redisTemplate.opsForHash().increment(statsKey, "queuedOrders", -1);
            }
            case "failure" -> {
                redisTemplate.opsForHash().increment(statsKey, "failedOrders", 1);
                redisTemplate.opsForHash().increment(statsKey, "queuedOrders", -1);
            }
        }
        
        // Set TTL for stats
        redisTemplate.expire(statsKey, Duration.ofDays(7));
    }
    
    /**
     * Initialize flash sale statistics
     */
    private void initializeFlashSaleStats(String flashSaleId) {
        String statsKey = FLASH_SALE_STATS_KEY + flashSaleId;
        
        Map<String, Object> initialStats = new HashMap<>();
        initialStats.put("totalOrders", 0L);
        initialStats.put("successfulOrders", 0L);
        initialStats.put("failedOrders", 0L);
        initialStats.put("queuedOrders", 0L);
        initialStats.put("totalRevenue", 0.0);
        initialStats.put("averageOrderValue", 0.0);
        initialStats.put("peakOrdersPerSecond", 0L);
        initialStats.put("startTime", System.currentTimeMillis());
        
        redisTemplate.opsForHash().putAll(statsKey, initialStats);
        redisTemplate.expire(statsKey, Duration.ofDays(7));
    }
    
    /**
     * Generate flash sale statistics from database
     */
    private FlashSaleStats generateFlashSaleStats(String flashSaleId) {
        List<Order> flashSaleOrders = orderRepository.findByFlashSaleId(flashSaleId);
        
        long totalOrders = flashSaleOrders.size();
        long successfulOrders = flashSaleOrders.stream()
            .mapToLong(order -> order.getStatus() == OrderStatus.CONFIRMED || 
                              order.getStatus() == OrderStatus.DELIVERED ? 1 : 0)
            .sum();
        
        return FlashSaleStats.builder()
            .flashSaleId(flashSaleId)
            .totalOrders(totalOrders)
            .successfulOrders(successfulOrders)
            .failedOrders(totalOrders - successfulOrders)
            .queuedOrders(getQueueSize(flashSaleId))
            .isActive(isFlashSaleActive(flashSaleId))
            .build();
    }
    
    /**
     * Get current queue size
     */
    private Long getQueueSize(String flashSaleId) {
        String queueKey = FLASH_SALE_QUEUE_KEY + flashSaleId;
        return redisTemplate.opsForList().size(queueKey);
    }
    
    /**
     * Start queue processing
     */
    private void startQueueProcessing(String flashSaleId) {
        FlashSaleEvent event = new FlashSaleEvent(flashSaleId);
        processFlashSaleQueue(event);
    }
    
    /**
     * Process remaining queue items when stopping
     */
    private void processRemainingQueue(String flashSaleId) {
        // Implementation to process remaining items in queue
        log.info("Processing remaining queue items for flash sale: {}", flashSaleId);
    }
    
    /**
     * Get processing rate
     */
    private double getProcessingRate(String flashSaleId) {
        // Calculate orders processed per second
        return 100.0; // Placeholder
    }
    
    /**
     * Calculate estimated wait time
     */
    private long calculateEstimatedWaitTime(Long queueSize) {
        if (queueSize == null || queueSize == 0) {
            return 0;
        }
        // Assuming 100 orders per second processing rate
        return queueSize / 100;
    }
    
    // Helper methods for type conversion
    private Long getLongValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? Long.valueOf(value.toString()) : 0L;
    }
    
    private Double getDoubleValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? Double.valueOf(value.toString()) : 0.0;
    }
}