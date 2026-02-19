# Twitter-Style Rate Limiter

A production-ready Spring Boot application demonstrating distributed rate limiting using **Token Buckets** (Bucket4j + Redis).

## Features
-   **Viral Spike Protection**: Allows users to "burst" (e.g., post 5 tweets instantly) while enforcing a long-term limit.
-   **Hierarchical Limits**:
    -   **Global**: Protects the system from DDoS.
    -   **User/IP**: Limits based on plan (Free vs. Premium).
-   **Distributed State**: Uses Redis to share limits across multiple application instances.
-   **Observability**: Exposes standard headers (`X-Rate-Limit-Remaining`, `Retry-After`).

## Prerequisite
-   Docker & Docker Compose
-   Java 17+

## Quick Start

1.  **Start Redis**:
    ```bash
    docker-compose up -d
    ```

2.  **Run Application**:
    ```bash
    mvn spring-boot:run
    ```

## API Usage

### Free Tier (Default)
Limits: 5 Burst, 20 per Hour.
```bash
curl -v -X POST http://localhost:8080/api/tweets -d "Hello"
```
*Response Headers*:
-   `X-Rate-Limit-Remaining: 4`

### Premium Tier
Limits: 20 Burst, 100 per Hour.
```bash
curl -v -X POST http://localhost:8080/api/tweets \
  -H "X-API-KEY: PREMIUM-ALICE" \
  -d "Hello Premium"
```

### Handling Rate Limits
When you exceed the limit, you get a `429 Too Many Requests`:
```
HTTP/1.1 429 
Retry-After: 58
Content-Length: 42
Too many requests. Please wait 58 seconds.
```

## Testing
Run the integration tests to see the burst logic in action:
```bash
mvn test
```
