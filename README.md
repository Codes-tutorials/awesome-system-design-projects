# 🚀 Awesome System Design Projects

A curated collection of production-ready system design implementations showcasing distributed systems, scalability patterns, and real-world architectural solutions.

## 📋 Table of Contents

- [Overview](#overview)
- [Projects](#projects)
- [Getting Started](#getting-started)
- [Technologies](#technologies)
- [Contributing](#contributing)

## 🎯 Overview

This repository contains hands-on implementations of various system design patterns and distributed system concepts. Each project demonstrates real-world scenarios with complete code, documentation, and deployment configurations.

## 📦 Projects

### Rate Limiting & Traffic Control

#### 1. [Distributed API Rate Limiter](./distributed-api-rate-limiter)
Scalable API rate limiting using Redis and distributed algorithms.
- **Tech Stack**: Spring Boot, Redis, Token Bucket Algorithm
- **Key Features**: Multi-tenant support, distributed coordination, configurable limits

#### 2. [Distributed Rate Limiter](./distributed-rate-limiter)
General-purpose distributed rate limiting system.
- **Tech Stack**: Java, Redis, Sliding Window
- **Key Features**: High throughput, low latency, fault tolerance

#### 3. [Twitter Rate Limiter](./twitter-rate-limiter)
Twitter-style rate limiting with user-based quotas.
- **Tech Stack**: Spring Boot, Redis
- **Key Features**: Per-user limits, time-window based, API throttling

#### 4. [Retry Safe Rate Limiter](./retry-safe-rate-limiter)
Rate limiter with built-in retry logic and idempotency.
- **Tech Stack**: Spring Boot, Redis
- **Key Features**: Retry handling, idempotent operations, graceful degradation

### System Design Classics

#### 5. [Elevator System Design](./elevator-system-design)
Smart elevator dispatching and scheduling system.
- **Tech Stack**: Java, Spring Boot
- **Key Features**: Optimal scheduling, multi-elevator coordination, request queuing

#### 6. [Parking Lot System Design](./parking-lot-system-design)
Automated parking lot management system.
- **Tech Stack**: Java, Spring Boot
- **Key Features**: Spot allocation, payment processing, capacity management

#### 7. [Vending Machine System Design](./vending-machine-system-design)
State machine-based vending machine implementation.
- **Tech Stack**: Java, Design Patterns
- **Key Features**: State management, inventory tracking, payment handling

#### 8. [Hotel Management System](./hotel-management-system)
Complete hotel booking and management platform.
- **Tech Stack**: Spring Boot, PostgreSQL, Docker
- **Key Features**: Room booking, guest management, payment processing, amenities

### Distributed Systems & Scalability

#### 9. [Distributed Logging System](./distributed-logging-system)
Centralized logging solution for distributed applications.
- **Tech Stack**: Spring Boot, Kafka, Elasticsearch
- **Key Features**: Log aggregation, real-time processing, search capabilities

#### 10. [Kafka Batch Producer Demo](./kafka-batch-producer-demo)
High-throughput Kafka producer with batching optimization.
- **Tech Stack**: Spring Boot, Apache Kafka
- **Key Features**: Batch processing, performance optimization, error handling

#### 11. [Weibo Hot List Demo](./weibo-hot-list-demo)
Real-time trending topics system (Weibo-style).
- **Tech Stack**: Spring Boot, Redis
- **Key Features**: Real-time ranking, score calculation, cache optimization

### Data Management & Consistency

#### 12. [Redis Cache Consistency Demo](./redis-cache-consistency-demo)
Cache consistency patterns and strategies.
- **Tech Stack**: Spring Boot, Redis, MySQL
- **Key Features**: Cache-aside, write-through, cache invalidation strategies

#### 13. [Large Table Update Demo](./large-table-update-demo)
Efficient strategies for updating large database tables.
- **Tech Stack**: Spring Boot, MySQL
- **Key Features**: Batch updates, pagination, performance optimization

#### 14. [Shared DB Refactoring](./shared-db-refactoring)
Migrating from shared database to microservices architecture.
- **Tech Stack**: Spring Boot, PostgreSQL
- **Key Features**: Database per service, data migration, eventual consistency

### API Design & Reliability

#### 15. [Interface Anti-Shake Demo](./interface-anti-shake-demo)
Preventing duplicate API submissions and request debouncing.
- **Tech Stack**: Spring Boot, Redis, AOP
- **Key Features**: Idempotency, duplicate detection, token-based validation

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Docker & Docker Compose
- Redis (for applicable projects)
- PostgreSQL/MySQL (for applicable projects)

### Quick Start

1. Clone the repository:
```bash
git clone https://github.com/Codes-tutorials/awesome-system-design-projects.git
cd awesome-system-design-projects
```

2. Navigate to any project:
```bash
cd <project-name>
```

3. Follow the project-specific README for setup instructions.

### Common Setup Pattern

Most projects follow this structure:

```bash
# Build the project
mvn clean install

# Run with Docker (if docker-compose.yml exists)
docker-compose up -d

# Run the application
mvn spring-boot:run
```

## 🛠 Technologies

- **Languages**: Java
- **Frameworks**: Spring Boot, Spring Cloud
- **Databases**: PostgreSQL, MySQL, Redis
- **Message Queues**: Apache Kafka
- **Containerization**: Docker, Docker Compose
- **Build Tools**: Maven
- **Design Patterns**: Singleton, Factory, Strategy, State Machine, Observer

## 📚 Learning Path

### Beginner
1. Elevator System Design
2. Parking Lot System Design
3. Vending Machine System Design

### Intermediate
1. Hotel Management System
2. Redis Cache Consistency Demo
3. Interface Anti-Shake Demo

### Advanced
1. Distributed Rate Limiter
2. Distributed Logging System
3. Kafka Batch Producer Demo
4. Shared DB Refactoring

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

Each project may have its own license. Please refer to individual project directories for specific licensing information.

## 🌟 Star History

If you find these projects helpful, please consider giving this repository a star!

## 📞 Contact

For questions or suggestions, please open an issue in the repository.

---

**Happy Learning! 🎓**
