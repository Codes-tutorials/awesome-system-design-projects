# E-commerce Order Service - Low Level Design (LLD)

## 1. System Components Overview

The Order Service is designed with a layered architecture following Domain-Driven Design (DDD) principles and microservices patterns. The system handles millions of concurrent requests through advanced caching, sharding, and asynchronous processing.

## 2. Package Structure

```
com.ecommerce.order/
├── config/                     # Configuration classes
│   ├── DatabaseConfig.java
│   ├── KafkaConfig.java
│   ├── RedisConfig.java
│   ├── HazelcastConfig.java
│   ├── SecurityConfig.java
│   └── AsyncConfig.java
├── controller/                 # REST API controllers
│   ├── OrderController.java
│   ├── FlashSaleController.java
│   └── AdminController.java
├── service/                    # Business logic services
│   ├── OrderService.java
│   ├── FlashSaleService.java
│   ├── InventoryService.java
│   ├── PaymentService.java
│   ├── NotificationService.java
│   └── RateLimitService.java
├── repository/                 # Data access layer
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── FlashSaleRepository.java
├── model/                      # Domain entities
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   └── OrderPriority.java
├── dto/                        # Data transfer objects
│   ├── CreateOrderRequest.java
│   ├── OrderResponse.java
│   ├── UpdateOrderRequest.java
│   └── FlashSaleRequest.java
├── event/                      # Event models for Kafka
│   ├── OrderEvent.java
│   ├── OrderEventType.java
│   ├── PaymentEvent.java
│   └── InventoryEvent.java
├── exception/                  # Custom exceptions
│   ├── OrderNotFoundException.java
│   ├── InsufficientInventoryException.java
│   ├── PaymentFailedException.java
│   └── RateLimitExceededException.java
├── util/                       # Utility classes
│   ├── ShardingUtil.java
│   ├── OrderNumberGenerator.java
│   └── ValidationUtil.java
└── aspect/                     # Cross-cutting concerns
    ├── LoggingAspect.java
    ├── MetricsAspect.java
    └── RateLimitAspect.java
```

## 3. Database Design

### 3.1 Database Schema

#### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) UNIQUE NOT NULL,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(50),
    payment_id VARCHAR(100),
    shipping_address_id VARCHAR(36),
    billing_address_id VARCHAR(36),
    discount_amount DECIMAL(19,2) DEFAULT 0,
    tax_amount DECIMAL(19,2) DEFAULT 0,
    shipping_amount DECIMAL(19,2) DEFAULT 0,
    notes TEXT,
    source VARCHAR(20) DEFAULT 'WEB',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    is_flash_sale BOOLEAN DEFAULT FALSE,
    flash_sale_id VARCHAR(36),
    shard_key INTEGER NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT
);

-- Indexes for performance
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_shard ON orders(shard_key);
CREATE INDEX idx_order_flash_sale ON orders(flash_sale_id) WHERE flash_sale_id IS NOT NULL;
```

#### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    brand VARCHAR(100),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(19,2) NOT NULL CHECK (total_price >= 0),
    weight DECIMAL(10,3),
    dimensions VARCHAR(50),
    is_digital BOOLEAN DEFAULT FALSE,
    is_gift_wrap BOOLEAN DEFAULT FALSE,
    gift_message TEXT,
    special_instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_sku ON order_items(sku);
```

### 3.2 Sharding Strategy

The system uses horizontal sharding based on user_id hash to distribute load across multiple database instances:

```java
public class ShardingUtil {
    private static final int SHARD_COUNT = 16;
    
    public static int calculateShardKey(String userId) {
        return Math.abs(userId.hashCode()) % SHARD_COUNT;
    }
    
    public static String getShardedTableName(String baseTableName, String userId) {
        int shardKey = calculateShardKey(userId);
        return baseTableName + "_" + shardKey;
    }
}
```

## 4. API Design

### 4.1 REST Endpoints

#### Order Management APIs
```java
@RestController
@RequestMapping("/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request);
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId);
    
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String orderId, 
                                                   @Valid @RequestBody UpdateOrderRequest request);
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId, 
                                          @RequestParam String reason);
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponse>> getUserOrders(@PathVariable String userId,
                                                           @PageableDefault Pageable pageable);
    
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable String orderId);
    
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> shipOrder(@PathVariable String orderId);
    
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable String orderId);
}
```

#### Flash Sale APIs
```java
@RestController
@RequestMapping("/flash-sales")
public class FlashSaleController {
    
    @PostMapping("/{flashSaleId}/orders")
    @RateLimit(key = "flash-sale", limit = 10000, window = 60)
    public ResponseEntity<OrderResponse> createFlashSaleOrder(@PathVariable String flashSaleId,
                                                            @Valid @RequestBody CreateOrderRequest request);
    
    @GetMapping("/{flashSaleId}/stats")
    public ResponseEntity<FlashSaleStats> getFlashSaleStats(@PathVariable String flashSaleId);
}
```

### 4.2 Request/Response Models

#### OrderResponse
```java
public class OrderResponse {
    private String orderId;
    private String orderNumber;
    private String userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingNumber;
    private Map<String, Object> metadata;
}
```

## 5. Service Layer Design

### 5.1 Order Service Implementation

```java
@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Async("orderProcessingExecutor")
    public CompletableFuture<OrderResponse> createOrderAsync(CreateOrderRequest request) {
        // 1. Validate request
        validateOrderRequest(request);
        
        // 2. Check inventory availability
        checkInventoryAvailability(request.getItems());
        
        // 3. Reserve inventory
        reserveInventory(request.getItems());
        
        // 4. Create order entity
        Order order = createOrderEntity(request);
        
        // 5. Save order to database
        Order savedOrder = orderRepository.save(order);
        
        // 6. Process payment asynchronously
        processPaymentAsync(savedOrder);
        
        // 7. Publish order created event
        publishOrderEvent(savedOrder, OrderEventType.ORDER_CREATED);
        
        // 8. Cache order for quick access
        cacheOrder(savedOrder);
        
        return CompletableFuture.completedFuture(mapToResponse(savedOrder));
    }
    
    @CircuitBreaker(name = "inventory-service", fallbackMethod = "fallbackInventoryCheck")
    @Retry(name = "inventory-service")
    @TimeLimiter(name = "inventory-service")
    private void checkInventoryAvailability(List<OrderItemRequest> items) {
        // Implementation with circuit breaker and retry
    }
    
    private void fallbackInventoryCheck(List<OrderItemRequest> items, Exception ex) {
        // Fallback logic - use cached inventory data
        log.warn("Inventory service unavailable, using cached data", ex);
    }
}
```

### 5.2 Flash Sale Service

```java
@Service
public class FlashSaleService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;
    private final RateLimitService rateLimitService;
    
    @RateLimit(key = "flash-sale-#{#flashSaleId}", limit = 10000, window = 60)
    public OrderResponse createFlashSaleOrder(String flashSaleId, CreateOrderRequest request) {
        // 1. Check flash sale validity
        validateFlashSale(flashSaleId);
        
        // 2. Apply rate limiting per user
        rateLimitService.checkUserRateLimit(request.getUserId(), "flash-sale");
        
        // 3. Queue order for processing
        queueFlashSaleOrder(flashSaleId, request);
        
        // 4. Return immediate response
        return createPendingOrderResponse(request);
    }
    
    private void queueFlashSaleOrder(String flashSaleId, CreateOrderRequest request) {
        String queueKey = "flash-sale-queue:" + flashSaleId;
        redisTemplate.opsForList().leftPush(queueKey, request);
    }
    
    @EventListener
    @Async("flashSaleExecutor")
    public void processFlashSaleQueue(FlashSaleEvent event) {
        String queueKey = "flash-sale-queue:" + event.getFlashSaleId();
        
        while (true) {
            CreateOrderRequest request = (CreateOrderRequest) 
                redisTemplate.opsForList().rightPop(queueKey, Duration.ofSeconds(1));
            
            if (request == null) break;
            
            try {
                orderService.createOrderAsync(request);
            } catch (Exception e) {
                handleFlashSaleOrderFailure(request, e);
            }
        }
    }
}
```