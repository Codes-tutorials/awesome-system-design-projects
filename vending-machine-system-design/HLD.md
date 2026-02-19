# High Level Design (HLD) - Vending Machine System

## 1. System Overview
The Vending Machine System is a software solution to manage the operations of a vending machine. It handles product selection, payment processing (coins/notes), inventory management, and item dispensing.

## 2. Requirements

### 2.1 Functional Requirements
1.  **Select Product**: Users can select a product from the available inventory.
2.  **Make Payment**: Users can insert coins or notes.
3.  **Validation**: The system must validate the inserted amount against the product price.
4.  **Dispense Product**: If sufficient funds are provided, the system dispenses the product.
5.  **Return Change**: The system must return the remaining balance if the payment exceeds the price.
6.  **Refund**: Users can cancel the request and get a full refund before dispensing.
7.  **Admin Operations**:
    - Add/Remove products.
    - Restock inventory.
    - Collect cash.

### 2.2 Non-Functional Requirements
1.  **Consistency**: Inventory and cash levels must be consistent (ACID properties).
2.  **Availability**: The system should be always available for user interaction.
3.  **Concurrency**: Handle multiple requests (though a physical machine is usually single-user, the backend might manage multiple machines).

## 3. Architecture
The system follows a **Multi-Module Spring Boot Architecture**:

-   **Web Layer (`vending-machine-web`)**: Exposes REST APIs for User and Admin interactions.
-   **Service Layer (`vending-machine-service`)**: Contains business logic for state management, payment processing, and inventory control.
-   **Model Layer (`vending-machine-model`)**: Defines the data entities (Product, Inventory, Cash) and repositories.

## 4. API Design

### User APIs
-   `POST /api/v1/vending/select/{productId}`: Select an item.
-   `POST /api/v1/vending/insert-money`: Insert coin/note.
-   `POST /api/v1/vending/dispense`: Confirm purchase.
-   `POST /api/v1/vending/cancel`: Cancel and refund.

### Admin APIs
-   `POST /api/v1/admin/products`: Add new product.
-   `PUT /api/v1/admin/inventory/{productId}`: Restock product.
-   `GET /api/v1/admin/cash`: Check total cash collected.
