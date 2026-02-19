# Distributed API Rate Limiter

A high-performance rate limiting solution for distributed systems (e.g., microservices behind a load balancer).

## Features
-   **Global Limit Enforcement**: 100 requests/minute per user across all instances.
-   **Redis Lua Script**: Guarantees atomicity and prevents race conditions.
-   **Hybrid Caching**: Uses local in-memory cache to block abusive users without hitting Redis (reduces network I/O).

## Setup & Run

1.  **Start Redis**:
    ```bash
    docker-compose up -d
    ```

2.  **Run Application**:
    ```bash
    mvn spring-boot:run
    ```

## Testing

### Manual Test
```bash
# Valid Request
curl -H "X-User-Id: alice" http://localhost:8080/api/resource
# Output: Success! You are within the rate limit.

# Spamming (Use Apache Bench or similar)
# ab -n 120 -c 10 -H "X-User-Id: alice" http://localhost:8080/api/resource
```

### Integration Test
Run the `LoadSimulationTest` to verify that even with 20 concurrent threads, the limit holds at ~100.
```bash
mvn test
```

## How It Works
1.  **Token Bucket**: We use a Token Bucket algorithm implemented in Lua.
2.  **Local Optimization**: If a user is blocked by Redis, the application servers cache this "blocked state". Subsequent requests during the block period are rejected locally, saving Redis round-trips.
