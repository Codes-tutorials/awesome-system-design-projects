# Elevator System Design

A simulation of a multi-elevator control system using the **LOOK Algorithm** (also known as the Elevator Algorithm).

## Core Components

1.  **Elevator Model (`Elevator.java`)**:
    -   Maintains current floor, direction, and a set of stops.
    -   Uses `TreeSet` (NavigableSet) to keep stops sorted:
        -   **Upward stops**: Ascending order.
        -   **Downward stops**: Descending order.
    -   **LOOK Algorithm**: The elevator continues in the current direction as long as there are requests in that direction. If none, it reverses or becomes IDLE.

2.  **Dispatcher (`DispatcherService.java`)**:
    -   Manages a fleet of elevators (default: 3).
    -   **Assignment Logic**: Calculates a "cost" for each elevator to handle a new request.
        -   Cost = Distance (if moving towards or IDLE).
        -   Cost = Distance + Penalty (if moving away).
    -   Assigns the request to the elevator with the lowest cost.

3.  **Controller (`ElevatorSystemApplication.java`)**:
    -   Provides a CLI to simulate the system.
    -   Commands: `step` (advance time), `req <src> <dest>` (add passenger).

## How to Run

1.  **Build**:
    ```bash
    mvn clean package
    ```

2.  **Run**:
    ```bash
    mvn spring-boot:run
    ```

3.  **Interact**:
    ```text
    > req 0 5
    > step
    > step
    ...
    ```
