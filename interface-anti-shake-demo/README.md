# Interface Anti-Shake and Anti-Duplicate Demo

This project demonstrates an advanced solution for preventing duplicate interface submissions using **Spring Boot**, **Redis**, **Lua Scripts**, and **AOP**, as inspired by [this Juejin article](https://juejin.cn/post/7588745319400734720).

## Features

-   **Anti-Shake (Debounce)**: Prevents high-frequency requests within a short time window.
-   **Anti-Duplicate (Idempotency)**: Ensures the same request (User + Method + Params) is processed only once within the specified timeout.
-   **Distributed Support**: Uses Redis + Lua for atomic operations, suitable for distributed/cluster environments.
-   **Non-Intrusive**: Implemented via `@AntiDuplicateSubmit` annotation and AOP.

## Prerequisites

-   Java 17+
-   Redis (Running on localhost:6380 by default, configurable in `application.properties`)

## Getting Started

1.  **Start Redis**:
    If you have Docker installed, you can use the provided compose file:
    ```bash
    docker-compose up -d
    ```
    Or ensure a Redis server is running on port 6380.

2.  **Build and Run**:
    ```bash
    mvn clean install
    java -jar target/interface-anti-shake-demo-0.0.1-SNAPSHOT.jar
    ```

## Usage

Annotate any controller method you want to protect with `@AntiDuplicateSubmit`.

### Example

```java
@PostMapping("/submit")
@AntiDuplicateSubmit(timeout = 5, unit = TimeUnit.SECONDS, message = "Do not submit repeatedly!")
public String submitOrder(@RequestBody String orderData) {
    return "Order submitted!";
}
```

### Testing

1.  **Normal Request**:
    ```bash
    curl -X POST "http://localhost:8085/api/test/submit" \
         -H "Content-Type: text/plain" \
         -d "Order123"
    ```
    *Response*: `Order submitted successfully!`

2.  **Duplicate Request (within 5s)**:
    Repeat the above command immediately.
    *Response*:
    ```json
    {
      "code": 429,
      "message": "Do not submit repeatedly!"
    }
    ```

## Project Structure

-   `annotation/`: Custom annotation `@AntiDuplicateSubmit`.
-   `aspect/`: AOP aspect `AntiDuplicateSubmitAspect` that intercepts requests and executes the Lua script.
-   `lua/`: `anti_duplicate_submit.lua` script for atomic Redis check-and-set.
-   `exception/`: Global exception handling for `DuplicateSubmitException`.
-   `controller/`: Test controller.
