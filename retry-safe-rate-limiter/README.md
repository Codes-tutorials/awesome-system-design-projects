# Retry-Safe Rate Limiter

This project demonstrates how to handle **legitimate retries** during traffic spikes without unfairly penalizing users.

## The Problem
In a standard Token Bucket rate limiter, if a user sends a request and the server fails (e.g., database timeout, internal error), the token is consumed.
If the user retries, they consume another token.
If the server is unstable (flaky), a user might exhaust their entire quota just trying to get one successful response. This leads to **Double Penalization**:
1.  They experienced a failure.
2.  They are now rate-limited.

## The Solution: Token Refunds
We implement a mechanism where **tokens are refunded** if the request fails due to a server-side error (HTTP 5xx).

### Key Components

1.  **`RateLimitInterceptor`**:
    -   **Pre-Handle**: Acquires a token from Redis.
    -   **After-Completion**: Checks the response status. If it is `5xx`, it calls `refundToken()`.

2.  **Lua Scripts**:
    -   `acquire_token.lua`: Standard token bucket logic.
    -   `refund_token.lua`: Atomically increments the token count (capped at capacity).

## How to Run

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
1.  **Happy Path**:
    ```bash
    curl -H "X-User-Id: test" http://localhost:8080/api/resource
    ```
    *Result*: Consumes 1 token.

2.  **Failure Path (Refund)**:
    ```bash
    curl -v -H "X-User-Id: test" "http://localhost:8080/api/resource?fail=true"
    ```
    *Result*: Returns 500, but the token is refunded. You can verify this by checking Redis or observing that you can make more requests than the limit would normally allow.

3.  **Flaky Endpoint**:
    ```bash
    curl -v -H "X-User-Id: test" http://localhost:8080/api/flaky
    ```
    *Result*: Randomly fails (70% chance). Even if it fails 10 times in a row, the user is not blocked because tokens are refunded each time.
