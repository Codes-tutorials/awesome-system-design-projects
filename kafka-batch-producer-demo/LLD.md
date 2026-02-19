# Low-Level Design (LLD)

## Class Diagram

```mermaid
classDiagram
    class EventController {
        +sendEvent(userId, message)
        +sendBulkEvents(userId, count)
    }
    
    class KafkaProducerService {
        -KafkaTemplate kafkaTemplate
        +sendMessage(userId, message)
    }
    
    class KafkaConsumerService {
        +listen(messages, partitions, offsets, keys)
    }
    
    class KafkaConfig {
        +userEventsTopic() NewTopic
    }
    
    EventController --> KafkaProducerService : uses
    KafkaProducerService --> KafkaTemplate : uses
    KafkaConsumerService ..> KafkaListener : annotated
```

## Sequence Diagram: Batch Sending

```mermaid
sequenceDiagram
    participant C as Client
    participant P as ProducerService
    participant K as KafkaClientLib
    participant B as KafkaBroker
    
    C->>P: sendBulkEvents(user1, 100)
    loop 100 times
        P->>K: send(topic, key=user1, msg)
        K-->>P: Future<Result>
        Note right of K: Added to Buffer
    end
    
    Note right of K: linger.ms expires or batch full
    K->>B: ProduceRequest(Batch of 100)
    B-->>K: ACK(Offset, Partition)
    K-->>P: Complete Futures
```

## Component Details

### 1. Producer Configuration (`application.properties`)

| Property | Value | Description |
| :--- | :--- | :--- |
| `spring.kafka.producer.batch-size` | `16384` | Default batch size in bytes (16KB). |
| `spring.kafka.producer.properties.linger.ms` | `10` | Wait time (ms) to group messages before sending. |
| `spring.kafka.producer.key-serializer` | `StringSerializer` | Serializes the `userId` key. |

### 2. Topic Configuration (`KafkaConfig.java`)

-   **Name**: `user-events`
-   **Partitions**: `3`
-   **Replicas**: `1` (Single broker setup)

### 3. Consumer Implementation (`KafkaConsumerService.java`)

-   **Type**: Batch Listener (`spring.kafka.listener.type=batch`).
-   **Signature**: Receives `List<String> messages` instead of a single string.
-   **Logging**: Logs the size of the received batch to verify batching behavior.

### 4. Error Handling

-   **Producer**: Uses `CompletableFuture.whenComplete` to log success or failure.
-   **Consumer**: Default error handler (logs error and continues). In production, a `DeadLetterPublishingRecoverer` should be used.
