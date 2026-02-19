# Low Level Design (LLD) - Parking Lot System

## 1. Class Diagram / Entities

### Enums
-   **VehicleType:** CAR, BIKE, TRUCK
-   **ParkingSpotType:** COMPACT, LARGE, MOTORBIKE, HANDICAPPED
-   **PaymentStatus:** PENDING, COMPLETED, FAILED
-   **SpotStatus:** FREE, OCCUPIED, RESERVED, OUT_OF_ORDER

### Core Models

#### 1. ParkingLot
-   `id`: String
-   `name`: String
-   `address`: Address
-   `floors`: List<ParkingFloor>
-   `entryGates`: List<Gate>
-   `exitGates`: List<Gate>

#### 2. ParkingFloor
-   `id`: String
-   `floorNumber`: int
-   `spots`: Map<ParkingSpotType, List<ParkingSpot>>
-   `displayBoard`: ParkingDisplayBoard

#### 3. ParkingSpot
-   `id`: String
-   `number`: String
-   `type`: ParkingSpotType
-   `status`: SpotStatus
-   `vehicle`: Vehicle (nullable)

#### 4. Vehicle
-   `licensePlate`: String
-   `type`: VehicleType

#### 5. Ticket
-   `id`: String
-   `vehicle`: Vehicle
-   `parkingSpot`: ParkingSpot
-   `entryTime`: LocalDateTime
-   `issuedBy`: Gate

#### 6. Payment
-   `id`: String
-   `ticketId`: String
-   `amount`: double
-   `status`: PaymentStatus
-   `transactionId`: String
-   `paymentTime`: LocalDateTime

#### 7. Gate
-   `id`: String
-   `gateNumber`: int
-   `type`: GateType (ENTRY, EXIT)
-   `operator`: ParkingAttendant

## 2. Services / Interfaces

### ParkingService
-   `Ticket entry(Vehicle vehicle, String gateId)`
-   `Payment exit(String ticketId, String gateId)`
-   `boolean isFull(VehicleType type)`

### ParkingStrategy (Interface)
-   `ParkingSpot findSpot(VehicleType type)`
-   *Implementations:* `NearestFirstStrategy`, `RandomStrategy`

### PricingService
-   `double calculateFee(Ticket ticket)`

## 3. Database Schema (Conceptual)

-   **parking_spots**: id, number, type, status, floor_id
-   **tickets**: id, vehicle_number, spot_id, entry_time, exit_time, amount
-   **payments**: id, ticket_id, amount, status

## 4. API Design (Controller)

-   `POST /api/v1/parking/entry`
    -   Body: `{ licensePlate, vehicleType, gateId }`
    -   Response: `{ ticketId, spotNumber, floorNumber, entryTime }`
-   `POST /api/v1/parking/exit`
    -   Body: `{ ticketId, gateId }`
    -   Response: `{ fee, paymentStatus }` (Simulating auto-payment or pre-payment check)
-   `POST /api/v1/parking/checkout`
    -   Body: `{ ticketId, amount, paymentMethod }`
    -   Response: `PaymentReceipt`

