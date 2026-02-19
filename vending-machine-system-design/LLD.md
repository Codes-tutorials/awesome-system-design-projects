# Low Level Design (LLD) - Vending Machine System

## 1. Domain Model

### Entities
1.  **Product**
    - `Long id`
    - `String name`
    - `BigDecimal price`
2.  **Inventory/Slot**
    - `Long id`
    - `Product product`
    - `int quantity`
    - `int locationId` (e.g., A1, B2)
3.  **Cash/Transaction**
    - `Coin` / `Note` Enums
    - `currentBalance`

### Enums
-   `Coin`: PENNY(0.01), NICKEL(0.05), DIME(0.1), QUARTER(0.25)
-   `Note`: ONE(1), FIVE(5), TEN(10), TWENTY(20)
-   `MachineState`: IDLE, READY, DISPENSING, OUT_OF_ORDER

## 2. Service Layer Components

### `InventoryService`
-   `getProduct(long id)`
-   `checkAvailability(long id)`
-   `updateInventory(long id, int quantity)`

### `PaymentService`
-   `addCoin(Coin coin)`
-   `addNote(Note note)`
-   `calculateChange(BigDecimal price, BigDecimal paid)`
-   `refund()`

### `VendingMachineStateService`
-   Manages the state transitions of the machine.
-   Uses the **State Design Pattern** (conceptually) or simple status checks.

## 3. Database Schema (H2 In-Memory)

```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(10,2)
);

CREATE TABLE inventory (
    id BIGINT PRIMARY KEY,
    product_id BIGINT,
    quantity INT,
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```

## 4. Class Structure

```java
public class VendingMachine {
    private InventoryService inventoryService;
    private PaymentService paymentService;
    private MachineState state;
    
    public void selectItem(Long productId) { ... }
    public void insertMoney(BigDecimal amount) { ... }
    public void dispenseItem() { ... }
}
```
