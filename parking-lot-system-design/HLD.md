# High Level Design (HLD) - Parking Lot System

## 1. Problem Statement
Design a parking lot system that can manage parking spots for different vehicle types, handle entry and exit of vehicles, calculate parking fees, and manage floors.

## 2. Requirements

### Functional Requirements
1.  **Multi-floor Parking:** The parking lot should have multiple floors.
2.  **Multiple Entry/Exit:** The system must support multiple entry and exit gates.
3.  **Vehicle Types:** Support for different types of vehicles (Car, Bike, Truck, etc.).
4.  **Ticket Generation:** A ticket is issued when a vehicle enters the parking lot.
5.  **Spot Allocation:** The system should assign a parking spot based on a strategy (e.g., nearest to the entrance).
6.  **Payment:** Users should be able to pay the parking fee upon exit.
7.  **Capacity Check:** The system should not allow entry if the parking lot is full for the specific vehicle type.

### Non-Functional Requirements
1.  **Consistency:** The system must ensure data consistency (e.g., no two vehicles assigned the same spot).
2.  **Availability:** The system should be highly available.
3.  **Scalability:** Should be able to handle a large number of concurrent requests (entry/exit).

## 3. System Architecture

### Components
1.  **Client (Entry/Exit Gate Terminals):** Interfaces used by parking attendants or automated kiosks.
2.  **Parking Service:** The core application handling business logic.
    -   **Gate Controller:** Manages entry and exit requests.
    -   **Spot Allocation Service:** Finds the best available spot.
    -   **Pricing Service:** Calculates fees based on duration and vehicle type.
3.  **Database:** Stores data about parking spots, tickets, payments, and history.

### Data Flow
1.  **Entry:**
    -   Vehicle arrives at Entry Gate.
    -   Gate Terminal requests `Parking Service` for a ticket.
    -   Service checks availability using `Spot Allocation Service`.
    -   If available, spot is reserved, Ticket is generated and saved to DB.
    -   Ticket is returned to the user.
2.  **Exit:**
    -   Vehicle arrives at Exit Gate.
    -   Ticket is scanned.
    -   Service calculates fee based on entry time and current time.
    -   User makes payment.
    -   Spot is freed.

## 4. Tech Stack
-   **Language:** Java 17+
-   **Framework:** Spring Boot (Multi-module)
-   **Database:** H2 (In-memory for demo) / MySQL (Production)
-   **Build Tool:** Maven
