# Shared Database Refactoring Demo

This project demonstrates how to refactor two microservices (A and B) that share a database to a decoupled, event-driven architecture **without downtime**.

## The Problem
-   **Service A (Owner)** and **Service B (Reader)** share the same database tables.
-   When Service A updates data, Service B might not see it immediately due to caching, or worse, Service B reads stale data because it's polling.
-   Direct database coupling prevents independent deployment and scaling.

## The Solution: Event-Driven Data Replication
Instead of Service B querying Service A's tables (or the shared DB), Service A publishes events whenever data changes. Service B listens to these events and updates its own **Read Model (Replica)**.

### Architecture
1.  **Service A (Owner)**:
    -   Manages the lifecycle of `User` entity.
    -   Writes to its own DB (simulated).
    -   Publishes `UserUpdatedEvent` to Kafka.
2.  **Service B (Reader)**:
    -   Maintains a `UserReplica` table (its own local copy).
    -   Consumes `UserUpdatedEvent` from Kafka.
    -   Updates `UserReplica` in near real-time.

## Zero-Downtime Migration Strategy

1.  **Step 1: Enable Dual Write / Publish (Service A)**
    -   Update Service A to publish events to Kafka *in addition* to writing to the DB.
    -   Deploy Service A. (No impact on B yet).

2.  **Step 2: Implement Consumer (Service B)**
    -   Update Service B to listen to Kafka and write to a *new* table (`user_replica`).
    -   Deploy Service B.

3.  **Step 3: Backfill Data**
    -   Run a script to publish events for all *existing* users in Service A so Service B's replica catches up.

4.  **Step 4: Switch Reads (Service B)**
    -   Update Service B's code to read from `user_replica` instead of the shared `users` table.
    -   Deploy Service B.

5.  **Step 5: Decouple**
    -   Revoke Service B's access to the shared `users` table.
    -   (Optional) Move `users` table to a separate physical database for Service A.

## Running the Demo

1.  **Start Kafka**:
    ```bash
    docker-compose up -d
    ```

2.  **Start Service A (Owner)** (Port 8081):
    ```bash
    cd owner-service
    mvn spring-boot:run
    ```

3.  **Start Service B (Reader)** (Port 8082):
    ```bash
    cd reader-service
    mvn spring-boot:run
    ```

4.  **Test the Sync**:
    -   Create/Update User in A:
        ```bash
        curl -X POST http://localhost:8081/users -H "Content-Type: application/json" -d '{"name":"Alice","email":"alice@example.com"}'
        ```
    -   Check Service B's Replica (It should have the data):
        ```bash
        curl http://localhost:8082/reader/users/1
        ```
