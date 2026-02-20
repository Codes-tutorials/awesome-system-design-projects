# Spring Boot Multi-Module Microservices Platform

## 🏗️ Architecture Overview

This project demonstrates a comprehensive microservices architecture using Spring Boot with the following key components:

- **Service Registry** (Eureka Server)
- **API Gateway** (Spring Cloud Gateway)
- **Circuit Breaker** (Resilience4j)
- **Event-Driven Architecture** (Apache Kafka)
- **Database per Service** (MySQL, PostgreSQL)

## 📋 Project Structure

```
microservices-platform/
├── service-registry/          # Eureka Service Registry
├── api-gateway/              # Spring Cloud Gateway
├── user-service/             # User Management Service (MySQL)
├── order-service/            # Order Management Service (PostgreSQL)
├── inventory-service/        # Inventory Management Service (MySQL)
├── notification-service/     # Notification Service (MongoDB)
├── common/                   # Shared utilities and DTOs
├── docker-compose.yml        # Infrastructure setup
└── README.md
```

## 🚀 Key Features

### 1. **Service Registry (Eureka)**
- Service discovery and registration
- Health monitoring
- Load balancing support

### 2. **API Gateway**
- Single entry point for all client requests
- Request routing and load balancing
- Rate limiting with Redis
- Circuit breaker integration
- CORS configuration

### 3. **Circuit Breaker (Resilience4j)**
- Fault tolerance and resilience
- Automatic fallback mechanisms
- Retry policies with exponential backoff
- Health indicators and metrics

### 4. **Event-Driven Architecture**
- Apache Kafka for asynchronous communication
- Event sourcing patterns
- Saga pattern for distributed transactions
- Dead letter queues for error handling

### 5. **Database per Service**
- **User Service**: MySQL database
- **Order Service**: PostgreSQL database
- **Inventory Service**: MySQL database
- **Notification Service**: MongoDB database

## 🛠️ Technology Stack

### Core Technologies
- **Spring Boot 3.2.1**: Application framework
- **Spring Cloud 2023.0.0**: Microservices toolkit
- **Java 17**: Programming language
- **Maven**: Build tool

### Infrastructure
- **Netflix Eureka**: Service registry
- **Spring Cloud Gateway**: API gateway
- **Apache Kafka**: Message broker
- **Redis**: Caching and rate limiting
- **Docker**: Containerization

### Databases
- **MySQL 8.0**: User and Inventory services
- **PostgreSQL 15**: Order service
- **MongoDB**: Notification service

### Monitoring & Observability
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **Spring Boot Actuator**: Health checks and metrics
- **Micrometer**: Application metrics

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### 1. Start Infrastructure Services
```bash
docker-compose up -d
```

This will start:
- MySQL (ports 3306, 3307)
- PostgreSQL (port 5432)
- MongoDB (port 27017)
- Apache Kafka (port 9092)
- Zookeeper (port 2181)
- Redis (port 6379)

### 2. Build All Services
```bash
mvn clean install
```

### 3. Start Services (in order)

#### Service Registry
```bash
cd service-registry
mvn spring-boot:run
```
Access: http://localhost:8761

#### API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```
Access: http://localhost:8080

#### User Service
```bash
cd user-service
mvn spring-boot:run
```
Access: http://localhost:8081

#### Order Service
```bash
cd order-service
mvn spring-boot:run
```
Access: http://localhost:8082

## 📡 API Endpoints

### User Service (via API Gateway)
```bash
# Create User
POST http://localhost:8080/api/users
{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "1234567890"
}

# Get User by ID
GET http://localhost:8080/api/users/1

# Get All Users
GET http://localhost:8080/api/users

# Update User
PUT http://localhost:8080/api/users/1
{
  "firstName": "John Updated",
  "phoneNumber": "9876543210"
}

# Delete User
DELETE http://localhost:8080/api/users/1

# Search Users
GET http://localhost:8080/api/users/search?name=John
```

### Order Service (via API Gateway)
```bash
# Create Order
POST http://localhost:8080/api/orders
{
  "userId": 1,
  "orderItems": [
    {
      "productId": 1,
      "productName": "Product 1",
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "shippingAddress": "123 Main St",
  "paymentMethod": "CREDIT_CARD"
}

# Get Order by ID
GET http://localhost:8080/api/orders/1

# Get Orders by User
GET http://localhost:8080/api/orders/user/1

# Update Order Status
PUT http://localhost:8080/api/orders/1/status
{
  "status": "CONFIRMED"
}
```

## 🔧 Configuration

### Service Ports
- **Service Registry**: 8761
- **API Gateway**: 8080
- **User Service**: 8081
- **Order Service**: 8082
- **Inventory Service**: 8083
- **Notification Service**: 8084

### Database Configuration
Each service uses its own database:

#### User Service (MySQL)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service_db
    username: root
    password: password
```

#### Order Service (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_service_db
    username: postgres
    password: password
```

## 🔄 Event-Driven Communication

### Kafka Topics
- **user-events**: User lifecycle events
- **order-events**: Order lifecycle events
- **inventory-events**: Inventory updates
- **notification-events**: Notification requests

### Event Examples

#### User Created Event
```json
{
  "eventId": "uuid",
  "eventType": "USER_CREATED",
  "timestamp": "2024-01-13T10:30:00Z",
  "source": "user-service",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Order Created Event
```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "timestamp": "2024-01-13T10:30:00Z",
  "source": "order-service",
  "orderId": 1,
  "userId": 1,
  "totalAmount": 59.98,
  "status": "PENDING"
}
```

## 🛡️ Resilience Patterns

### Circuit Breaker Configuration
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
```

### Retry Configuration
```yaml
resilience4j:
  retry:
    instances:
      user-service:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
```

## 📊 Monitoring

### Health Checks
- Service Registry: http://localhost:8761/actuator/health
- API Gateway: http://localhost:8080/actuator/health
- User Service: http://localhost:8081/actuator/health
- Order Service: http://localhost:8082/actuator/health

### Metrics
- Prometheus metrics: http://localhost:8080/actuator/prometheus
- Application metrics: http://localhost:8080/actuator/metrics

### Eureka Dashboard
Access the Eureka dashboard at http://localhost:8761 to see:
- Registered services
- Service health status
- Instance information

## 🐳 Docker Support

### Build Docker Images
```bash
# Build all services
mvn clean package

# Build individual service
cd user-service
mvn clean package
docker build -t user-service:latest .
```

### Docker Compose
```bash
# Start infrastructure
docker-compose up -d

# Start all services
docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### API Testing with curl
```bash
# Test user creation
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
  }'
```

## 🔍 Troubleshooting

### Common Issues

1. **Service not registering with Eureka**
   - Check Eureka server is running
   - Verify network connectivity
   - Check application.yml configuration

2. **Database connection issues**
   - Ensure Docker containers are running
   - Verify database credentials
   - Check port availability

3. **Kafka connection issues**
   - Verify Kafka and Zookeeper are running
   - Check topic creation
   - Verify bootstrap servers configuration

### Logs
```bash
# View service logs
docker-compose logs -f user-service

# View all logs
docker-compose logs -f
```

## 🚀 Production Deployment

### Environment-Specific Configuration
- **Development**: application.yml
- **Testing**: application-test.yml
- **Production**: application-prod.yml

### Security Considerations
- Enable HTTPS/TLS
- Implement JWT authentication
- Use secrets management
- Enable audit logging
- Configure firewall rules

### Scaling
- Horizontal scaling with multiple instances
- Load balancing with API Gateway
- Database read replicas
- Kafka partitioning for high throughput

## 📚 Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Netflix Eureka Documentation](https://github.com/Netflix/eureka)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Spring Boot, Spring Cloud, and modern microservices patterns**