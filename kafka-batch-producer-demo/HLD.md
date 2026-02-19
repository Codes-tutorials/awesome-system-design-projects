# High-Level Design (HLD)

## System Architecture

The system consists of a Spring Boot application acting as both a Producer and a Consumer (for demonstration), interacting with a Kafka Cluster.

```mermaid
graph TD
    Client[REST Client] -->|HTTP POST /send-bulk| Producer[Producer Service]
    
    subgraph "Kafka Client (Spring Boot)"
        Producer -->|Async Send| Buffer[Internal Buffer]
        Buffer -->|Batch Send (linger.ms / batch-size)| Broker[Kafka Broker]
    end
    
    subgraph "Kafka Cluster"
        Broker -->|Partition 0| P0[Partition 0]
        Broker -->|Partition 1| P1[Partition 1]
        Broker -->|Partition 2| P2[Partition 2]
    end
    
    subgraph "Consumer (Spring Boot)"
        Listener[Batch Listener] -->|Poll| Broker
        Listener -->|Process Batch| Logic[Business Logic]
    end
```

## Data Flow

1.  **Ingestion**: Client triggers an API call with `userId` and `message`.
2.  **Buffering**: The Producer Service sends the record to the Kafka Client library. The record is stored in a local buffer.
3.  **Batching**: The Kafka Client waits for `linger.ms` (10ms) or until `batch-size` (16KB) is full.
4.  **Transmission**: The batch is sent to the Kafka Broker.
5.  **Routing**: The Broker uses the `userId` hash to route the batch to a specific partition.
6.  **Consumption**: The Consumer polls the broker and receives a batch of records.

## Key Design Decisions

### 1. Partitioning Strategy
-   **Requirement**: Order preservation for user events.
-   **Solution**: Use `userId` as the message key. Kafka guarantees that all messages with the same key go to the same partition.

### 2. Batching Strategy
-   **Requirement**: High throughput.
-   **Solution**:
    -   `linger.ms = 10`: Adds a small latency to allow more records to accumulate.
    -   `batch-size = 16384`: Upper limit of batch size in bytes.
    -   Benefit: Reduces the number of network requests and I/O overhead.

### 3. Reliability
-   **Producer**: `acks=1` (default) is sufficient for this demo, but `acks=all` can be used for higher durability.
-   **Consumer**: `auto-offset-reset=earliest` ensures no data loss if the consumer starts late.
