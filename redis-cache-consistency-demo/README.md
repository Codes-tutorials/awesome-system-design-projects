# Redis Cache Consistency Demo

This project demonstrates different strategies for maintaining cache consistency between a Database (H2) and a Cache (Redis).

## Strategies Implemented

1.  **Bad Strategy**: Update DB but forget to update cache (simulates stale data).
2.  **Cache Aside / Write-Around**: Update DB and evict cache (Standard approach).
3.  **Delayed Double Deletion**: Delete Cache -> Update DB -> Delete Cache again (after delay). Handles high concurrency race conditions.
4.  **Async Invalidation (CDC Pattern)**: Update DB -> Transaction Commit -> Async Event triggers Cache Eviction.

## Prerequisites

- Java 17+
- Maven
- Docker (for Redis)

## How to Run

1.  Start Redis:
    ```bash
    docker-compose up -d
    ```

2.  Run the Application:
    ```bash
    mvn spring-boot:run
    ```

## API Endpoints to Test

- **Get Product**: `GET /products/1`
- **Bad Update**: `PUT /products/1/bad?price=100`
- **Evict Update**: `PUT /products/1/evict?price=200`
- **Double Delete**: `PUT /products/1/double-delete?price=300`
- **Async Update**: `PUT /products/1/async?price=400`
