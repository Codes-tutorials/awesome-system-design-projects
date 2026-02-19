# Hotel Management System

A production-ready implementation of a Hotel Management System using Spring Boot.

## Features

1.  **Search Available Rooms**: Find rooms by hotel, type, and date range.
2.  **Concurrency Control**: Uses Pessimistic Locking (`SELECT ... FOR UPDATE`) to prevent double bookings.
3.  **Dynamic Pricing**: Weekends are automatically priced 20% higher (simulated).
4.  **Async Notifications**: Decoupled email sending using Spring Events and `@Async`.
5.  **Data Models**: Hotels, Rooms, Guests, Reservations.

## API Endpoints

### 1. Search Rooms
`GET /api/bookings/search?hotelId=1&type=SINGLE&checkIn=2024-01-01&checkOut=2024-01-05`

### 2. Book a Room
`POST /api/bookings`
```json
{
  "roomId": 1,
  "checkInDate": "2024-01-01",
  "checkOutDate": "2024-01-05",
  "guest": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "1234567890"
  }
}
```

## Architecture Highlights

-   **Pessimistic Locking**: `RoomRepository.findByIdWithLock` ensures data integrity during concurrent booking attempts.
-   **Event-Driven Architecture**: `BookingCreatedEvent` decouples the core booking logic from side effects like notifications.
-   **Service Layer Separation**: `PricingService` isolates pricing rules from booking workflow.

## How to Run

```bash
mvn spring-boot:run
```
