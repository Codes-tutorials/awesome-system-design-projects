# Distributed Rate Limiter & Cache Consistency Solutions

This repository contains solutions for two system design interview questions:
1.  **Distributed Rate Limiter**: A complete Spring Boot + Redis implementation using the Token Bucket algorithm.
2.  **Distributed Cache Consistency**: A design overview and reference to the `redis-cache-consistency-demo` project.

---

## Part 1: Distributed Rate Limiter Project

### 1. Algorithm Choice: Token Bucket
I chose the **Token Bucket** algorithm implemented via **Redis Lua Scripts**.

*   **Why Token Bucket?**
    *   **Burst Handling**: Unlike Leaky Bucket (which smoothes traffic) or Fixed Window, Token Bucket allows a predefined "burst" (capacity) of traffic, which is crucial for good UX (e.g., loading a timeline triggers parallel requests).
    *   **Memory Efficiency**: We only store two numbers per user (tokens, last_refill_time), not a log of every request (like Sliding Window Log).
    *   **Accuracy**: It simulates a continuous flow better than Fixed Window.

### 2. Storage: Redis
*   **Why Redis?**
    *   **Distributed**: Essential for "multiple servers". Local memory (Guava) would fail to enforce global limits.
    *   **Performance**: In-memory speeds (sub-millisecond).
    *   **Atomicity**: Redis Lua scripts execute atomically, preventing race conditions.

### 3. Preventing Race Conditions
*   **The Problem**: If two servers read the token count at the same time, they might both decrement it, violating the limit.
*   **The Solution**: **Lua Scripts**. The logic (Refill -> Check -> Decrement) is executed as a single atomic operation in Redis. No other command can run in between.

### 4. Handling Bottlenecks
*   **Sharding**: Distribute keys across a Redis Cluster.
*   **Multi-Level Cache**: Use a small local cache (e.g., Caffeine) for extremely hot keys (like "global_limit") to reduce Redis round trips, with a small TTL (e.g., 100ms).

### 5. Graceful Degradation
*   When the limit is reached, we throw a `ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS)` which translates to a **429 Too Many Requests** HTTP response.

### 6. Follow-Ups & Additional Interview Answers

#### Q: How to ensure accuracy in sliding windows without degrading performance?
*   **Problem**: Sliding Window Log stores every timestamp, which is accurate but consumes high memory (O(N)) and requires scanning the sorted set (O(logN)), degrading performance.
*   **Solution**: Use **Sliding Window Counter (or Hybrid approach)**.
    *   Divide the time window into smaller "buckets" (e.g., 1-second buckets for a 1-minute window).
    *   Store a counter for each bucket.
    *   Sum the counters of the relevant buckets to get the total.
    *   **Approximation**: Calculate `Requests in Previous Window * (1 - Time into Current Window) + Requests in Current Window`.
    *   **Benefit**: Reduces memory to O(1) (fixed number of buckets) while maintaining 99% accuracy.

#### Q: Can you design it as a reusable library/service used by multiple teams?
*   **As a Library (Spring Boot Starter)**:
    *   Package the `RateLimiterService`, `RedisConfig`, and `@RateLimit` aspect into a jar.
    *   Allow configuration via `application.properties` (e.g., `ratelimit.redis.host`).
    *   Teams just add the dependency and use the annotation.
*   **As a Sidecar / Service Mesh (Envoy/Istio)**:
    *   For polyglot environments (Go, Python, Java), implement rate limiting in the **API Gateway** or **Service Mesh Sidecar**.
    *   The application code remains unaware of rate limiting logic.

#### Q: Dynamic Limits (Free vs Premium)?
*   The `@RateLimit` annotation can be enhanced to fetch limits from a database/config service based on the user's tier (Free/Premium) before calling the Redis service.
*   Example: `rate = configService.getLimit(userId)`.

#### Q: Multi-Region Deployments?
*   **Active-Active Redis (CRDB)**: Use Redis Enterprise or similar to sync counters across regions (eventual consistency).
*   **Partitioned Limits**: If global consistency is too slow, split the limit. If the global limit is 1000, and you have 2 regions, give each region 500.

### How to Run
1.  Start Redis: `docker-compose up -d`
2.  Run App: `mvn spring-boot:run`
3.  Test:
    *   `curl -H "User-Id: 1" http://localhost:8080/api/user-limited` (Limit: 5 burst)
    *   `curl http://localhost:8080/api/global-limited` (Limit: 20 burst)

---

## Part 2: Distributed Cache Consistency (Interview Question 1)

**Question**: "How do you keep your cache coherent without killing performance when multiple services update it?"

**Answer & Solution**:
The primary challenge is **stale data** (reading old data after a write) and **race conditions** (re-caching old data).

### Strategies (Implemented in `redis-cache-consistency-demo` project)

1.  **Cache Aside with Eviction (Standard)**:
    *   **Action**: On DB update, *delete* (evict) the cache key.
    *   **Why**: Deletion is safer than updating. If you update the cache, two concurrent writes might update it in the wrong order.

2.  **Delayed Double Deletion (High Concurrency)**:
    *   **Scenario**: Thread A deletes cache -> Thread A updates DB. Meanwhile, Thread B reads DB (stale) -> Thread B writes stale data to cache.
    *   **Fix**: Thread A deletes cache -> Updates DB -> **Waits X ms** -> Deletes cache *again*.
    *   **Implementation**: Use a scheduled task or message queue to trigger the second deletion.

3.  **Async Invalidation (CDC Pattern - Best for Microservices)**:
    *   **Action**: Services **only** update the DB.
    *   **Mechanism**: A separate process (using Debezium or a Message Queue like Kafka/RabbitMQ) listens to DB changes (CDC) and invalidates the relevant cache keys.
    *   **Benefit**: Decouples the service from cache logic. No performance penalty on the write path.

**Reference Project**: See `../redis-cache-consistency-demo` for the code implementation of these strategies.
