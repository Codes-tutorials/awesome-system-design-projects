# IPL Ticket Booking System - Architecture Deep Dive

## 🎯 System Overview

This document provides a comprehensive architectural overview of the IPL ticket booking system designed to handle **millions of concurrent users** during peak ticket sales.

## 🏗️ High-Level Architecture

```
                                    ┌─────────────────────────────────────┐
                                    │           Load Balancer             │
                                    │        (Nginx/AWS ALB)              │
                                    └─────────────────┬───────────────────┘
                                                      │
                    ┌─────────────────────────────────┼─────────────────────────────────┐
                    │                                 │                                 │
          ┌─────────▼─────────┐              ┌─────────▼─────────┐              ┌─────────▼─────────┐
          │   App Instance 1  │              │   App Instance 2  │              │   App Instance N  │
          │  (Spring Boot)    │              │  (Spring Boot)    │              │  (Spring Boot)    │
          └─────────┬─────────┘              └─────────┬─────────┘              └─────────┬─────────┘
                    │                                  │                                  │
                    └─────────────────────────────────┬┼─────────────────────────────────┘
                                                      ││
                    ┌─────────────────────────────────┘└─────────────────────────────────┐
                    │                                                                    │
          ┌─────────▼─────────┐                                              ┌─────────▼─────────┐
          │     PostgreSQL    │                                              │       Redis       │
          │   (Primary DB)    │                                              │  (Locks/Cache)    │
          │                   │                                              │                   │
          │ ┌───────────────┐ │                                              │ ┌───────────────┐ │
          │ │   Seats       │ │                                              │ │ Distributed   │ │
          │ │   Bookings    │ │                                              │ │ Locks         │ │
          │ │   Users       │ │                                              │ │ Rate Limits   │ │
          │ │   Matches     │ │                                              │ │ Cache         │ │
          │ └───────────────┘ │                                              │ └───────────────┘ │
          └───────────────────┘                                              └───────────────────┘
                    │                                                                    │
                    │                        ┌─────────────────┐                       │
                    └────────────────────────│     Kafka       │───────────────────────┘
                                             │ (Event Stream)  │
                                             │                 │
                                             │ ┌─────────────┐ │
                                             │ │ booking-    │ │
                                             │ │ events      │ │
                                             │ │ payment-    │ │
                                             │ │ events      │ │
                                             │ │ notification│ │
                                             │ │ -events     │ │
                                             │ └─────────────┘ │
                                             └─────────────────┘
```

## 🔄 Concurrency Control Strategy

### 1. **Multi-Layer Locking Mechanism**

```
Request Flow for Seat Booking:

User Request → Rate Limiting → Distributed Lock → Optimistic Lock → Database Update

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│Rate Limiter │───▶│Redis Lock   │───▶│JPA Version  │───▶│PostgreSQL   │
│   Request   │    │(Bucket4j)   │    │(Redisson)   │    │(@Version)   │    │Update       │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
      │                    │                    │                    │                    │
      │                    │                    │                    │                    │
   429 Error          Lock Failed         Version Conflict      Success/Failure
   Try Later          Seat Taken          Retry Required         Booking Created
```

### 2. **Rate Limiting Hierarchy**

```
Global System Limit: 50,000 bookings/minute
         │
         ├─ IP-based Limit: 50 bookings/minute per IP
         │        │
         │        ├─ User Limit: 5 bookings/minute per user
         │        │        │
         │        │        └─ Burst Protection: 1 booking/10 seconds
         │        │
         │        └─ Anonymous Limit: 10 bookings/minute per IP
         │
         └─ Circuit Breaker: Opens at 30% failure rate
```

## 🎯 Critical Components Deep Dive

### 1. **SeatLockingService - The Heart of Concurrency Control**

```java
/**
 * CRITICAL: This service prevents the "double booking" problem
 * Uses Redis distributed locks to coordinate across multiple app instances
 */
@Service
public class SeatLockingService {
    
    // Lock acquisition with timeout and lease
    public boolean lockSeats(List<Long> seatIds, Long userId) {
        String lockKey = generateLockKey(seatIds, userId);
        RLock lock = redissonClient.getLock(lockKey);
        
        // Try to acquire lock: wait 5s, hold for 10 minutes
        return lock.tryLock(5, 600, TimeUnit.SECONDS);
    }
    
    // Prevents deadlocks by sorting seat IDs
    private String generateLockKey(List<Long> seatIds, Long userId) {
        seatIds.sort(Long::compareTo);  // CRITICAL: Consistent ordering
        return "seat_lock:" + String.join(",", seatIds) + ":" + userId;
    }
}
```

**Why This Works:**
- **Distributed**: Works across multiple application instances
- **Deadlock Prevention**: Consistent lock ordering prevents circular waits
- **Timeout Protection**: Locks auto-expire to prevent permanent blocks
- **User Isolation**: Each user gets their own lock scope

### 2. **BookingService - Transaction Orchestration**

```java
@Service
@Transactional
public class BookingService {
    
    @Retryable(value = {OptimisticLockingFailureException.class}, maxAttempts = 3)
    public BookingResponse bookSeats(BookingRequest request) {
        
        // 1. Rate limiting check (fail fast)
        if (!rateLimitingService.isAllowed(request.getUserId(), "booking")) {
            throw new BookingException("Rate limit exceeded");
        }
        
        // 2. Acquire distributed locks (prevent race conditions)
        if (!seatLockingService.lockSeats(request.getSeatIds(), request.getUserId())) {
            throw new SeatNotAvailableException("Seats locked by another user");
        }
        
        try {
            // 3. Verify availability with optimistic locking
            List<Seat> seats = seatRepository.findByIdInAndMatchIdForUpdate(
                request.getSeatIds(), request.getMatchId());
            
            // 4. Double-check availability (race condition protection)
            for (Seat seat : seats) {
                if (!seat.isAvailable()) {
                    throw new SeatNotAvailableException("Seat " + seat.getSeatNumber() + " not available");
                }
            }
            
            // 5. Create booking and update seats atomically
            Booking booking = createBooking(request, seats);
            reserveSeats(seats, booking);
            
            // 6. Publish event for downstream processing
            publishBookingEvent(booking, "BOOKING_CREATED");
            
            return createBookingResponse(booking, seats);
            
        } finally {
            // Always release distributed locks
            seatLockingService.unlockSeats(request.getSeatIds(), request.getUserId());
        }
    }
}
```

**Transaction Flow:**
1. **Pre-validation**: Rate limiting and basic checks
2. **Lock Acquisition**: Distributed locking across instances
3. **Database Transaction**: Optimistic locking + atomic updates
4. **Event Publishing**: Async downstream processing
5. **Lock Release**: Always cleanup, even on failure

### 3. **RateLimitingService - Burst Traffic Protection**

```java
@Service
public class RateLimitingService {
    
    // User-level rate limiting
    public boolean isAllowed(Long userId, String operation) {
        String bucketKey = "rate_limit:user:" + userId + ":" + operation;
        
        Bucket bucket = proxyManager.builder()
            .build(bucketKey, () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1)))      // 5/minute
                .addLimit(Bandwidth.simple(1, Duration.ofSeconds(10)))     // Burst protection
                .build());
        
        return bucket.tryConsume(1);
    }
    
    // Global system protection
    public boolean isGloballyAllowed(String operation) {
        Bucket globalBucket = proxyManager.builder()
            .build("global:" + operation, () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.simple(50000, Duration.ofMinutes(1)))  // 50K/minute globally
                .addLimit(Bandwidth.simple(1000, Duration.ofSeconds(1)))   // 1K/second burst
                .build());
        
        return globalBucket.tryConsume(1);
    }
}
```

**Rate Limiting Strategy:**
- **Token Bucket Algorithm**: Allows bursts while maintaining average rate
- **Multi-Level Limits**: User, IP, and global limits
- **Distributed**: Uses Redis for coordination across instances
- **Graceful Degradation**: Fails open if rate limiting service is down

## 📊 Data Flow Architecture

### 1. **Booking Request Flow**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│Rate Limiter │───▶│   Service   │
│   (Mobile/  │    │  (Load      │    │ (Redis)     │    │  (Spring    │
│    Web)     │    │  Balancer)  │    │             │    │   Boot)     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                                  │
                                                                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Response   │◀───│   Cache     │◀───│  Database   │◀───│Distributed  │
│  (JSON)     │    │  (Redis)    │    │(PostgreSQL) │    │Lock (Redis) │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### 2. **Event Processing Flow**

```
Booking Created ──┐
                  │
Payment Processed ├──▶ Kafka Topics ──▶ Event Consumers ──▶ Downstream Services
                  │                                              │
Booking Cancelled ┘                                              ├─ Notification Service
                                                                 ├─ Analytics Service
                                                                 ├─ Audit Service
                                                                 └─ Reporting Service
```

## 🔧 Database Design for High Concurrency

### 1. **Seat Table Optimization**

```sql
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price DECIMAL(10,2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,          -- Optimistic locking
    locked_by_user BIGINT,                      -- Temporary lock
    locked_until TIMESTAMP,                     -- Lock expiration
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Critical indexes for performance
    CONSTRAINT unique_seat_per_match UNIQUE (match_id, seat_number)
);

-- Performance indexes
CREATE INDEX idx_seat_match_status ON seats (match_id, status);
CREATE INDEX idx_seat_lock_expiry ON seats (locked_until) WHERE locked_until IS NOT NULL;
CREATE INDEX idx_seat_availability ON seats (match_id, status) WHERE status = 'AVAILABLE';
```

### 2. **Connection Pool Configuration**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50              # Tune based on load testing
      minimum-idle: 10                   # Keep connections warm
      connection-timeout: 30000          # 30 seconds
      idle-timeout: 600000               # 10 minutes
      max-lifetime: 1800000              # 30 minutes
      leak-detection-threshold: 60000    # Detect connection leaks
```

## 🚀 Performance Optimizations

### 1. **Caching Strategy**

```
┌─────────────────────────────────────────────────────────────┐
│                    Caching Layers                           │
├─────────────────────────────────────────────────────────────┤
│ L1: Application Cache (Caffeine) - 30 seconds TTL          │
│     └─ Seat availability, Match details                    │
│                                                             │
│ L2: Redis Cache - 5 minutes TTL                           │
│     └─ User sessions, Rate limiting buckets               │
│                                                             │
│ L3: Database Query Cache - 10 minutes TTL                 │
│     └─ Match schedules, Stadium layouts                   │
└─────────────────────────────────────────────────────────────┘
```

### 2. **Async Processing**

```java
@Async
@EventListener
public void handleBookingCreated(BookingCreatedEvent event) {
    // Non-blocking operations
    CompletableFuture.allOf(
        sendConfirmationEmail(event.getBooking()),
        updateAnalytics(event.getBooking()),
        processPayment(event.getBooking())
    );
}
```

## 📈 Monitoring & Observability

### 1. **Key Metrics**

```
Business Metrics:
├─ Booking Success Rate (Target: >95%)
├─ Average Response Time (Target: <2s)
├─ Concurrent Users (Monitor: Real-time)
└─ Revenue per Minute (Track: Peak sales)

Technical Metrics:
├─ Database Connection Pool Utilization
├─ Redis Hit Ratio (Target: >90%)
├─ Rate Limit Violations
├─ Lock Contention Rate
└─ JVM Memory Usage
```

### 2. **Alerting Thresholds**

```yaml
alerts:
  high_response_time:
    threshold: 5000ms
    duration: 2m
  
  booking_failure_rate:
    threshold: 10%
    duration: 1m
  
  database_connections:
    threshold: 80%
    duration: 30s
  
  redis_memory:
    threshold: 85%
    duration: 1m
```

## 🔒 Security Architecture

### 1. **Authentication Flow**

```
Client Request ──▶ JWT Validation ──▶ Rate Limiting ──▶ Business Logic
      │                   │                  │                │
      │                   ▼                  ▼                ▼
   401 Error         403 Forbidden     429 Rate Limited   200 Success
```

### 2. **Input Validation**

```java
@PostMapping("/bookings")
public ResponseEntity<?> bookTickets(@Valid @RequestBody BookingRequest request) {
    // Validation annotations ensure:
    // - User ID is not null
    // - Match ID exists
    // - Seat IDs are valid (1-10 seats)
    // - No SQL injection in parameters
}
```

## 🎯 Scalability Patterns

### 1. **Horizontal Scaling**

```
Single Instance (Development):
└─ 1 App Server + 1 DB + 1 Redis

Production Cluster:
├─ 3+ App Servers (Load Balanced)
├─ PostgreSQL Primary + 2 Read Replicas
├─ Redis Cluster (3 Masters + 3 Slaves)
└─ Kafka Cluster (3 Brokers)
```

### 2. **Auto-Scaling Configuration**

```yaml
# Kubernetes HPA
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ipl-booking-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ipl-booking-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

## 🚨 Failure Scenarios & Recovery

### 1. **Database Failure**
- **Detection**: Health check fails
- **Response**: Circuit breaker opens, serve cached data
- **Recovery**: Automatic failover to read replica

### 2. **Redis Failure**
- **Detection**: Lock acquisition timeouts
- **Response**: Degrade to database-only locking
- **Recovery**: Redis cluster failover

### 3. **High Load Scenario**
- **Detection**: Response time > 5s
- **Response**: Activate burst rate limiting
- **Recovery**: Auto-scaling triggers

This architecture ensures the system can handle IPL-scale traffic while maintaining data consistency and providing excellent user experience! 🏏