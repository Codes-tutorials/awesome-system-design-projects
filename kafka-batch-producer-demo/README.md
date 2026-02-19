# Kafka Batch Producer Demo

This project demonstrates a Spring Boot application that uses Kafka for asynchronous messaging with **Producer-Side Batching** and **Key-Based Partitioning**.

## Key Features

1.  **Producer Batching**: Configured `batch-size` and `linger.ms` to optimize network throughput.
2.  **Key-Based Partitioning**: Messages are sent with a `userId` as the key, ensuring all messages for the same user go to the same partition (ordering guarantee).
3.  **Batch Consumer**: The consumer is configured to receive messages in batches (`List<String>`).
4.  **Dockerized Environment**: Kafka and Zookeeper setup via Docker Compose.

## Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven

## Getting Started

### 1. Start Kafka Infrastructure

```bash
docker-compose up -d
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

## API Usage

### Send a Single Message
```bash
curl -X POST "http://localhost:8080/api/events/send?userId=user123&message=HelloKafka"
```

### Send Bulk Messages (Trigger Batching)
This endpoint sends 100 messages rapidly. You should see them grouped in the consumer logs.
```bash
curl -X POST "http://localhost:8080/api/events/send-bulk?userId=user123&count=100"
```

## Configuration Highlights

`application.properties`:
```properties
# Wait up to 10ms to fill the batch
spring.kafka.producer.properties.linger.ms=10
# Batch size 16KB
spring.kafka.producer.batch-size=16384
```

## Architecture

See [HLD.md](HLD.md) and [LLD.md](LLD.md) for design details.
