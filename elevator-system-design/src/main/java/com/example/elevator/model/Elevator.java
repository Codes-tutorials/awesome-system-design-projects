package com.example.elevator.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

@Data
@Slf4j
public class Elevator {
    private int id;
    private int currentFloor;
    private Direction direction;
    private int capacity;
    
    // We use TreeSets to keep stops sorted. 
    // Upward stops are processed in ascending order.
    // Downward stops are processed in descending order.
    private NavigableSet<Integer> upStops;
    private NavigableSet<Integer> downStops;

    public Elevator(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.currentFloor = 0; // Ground floor
        this.direction = Direction.IDLE;
        this.upStops = new TreeSet<>();
        this.downStops = new TreeSet<>(Collections.reverseOrder());
    }

    public void addStop(int floor) {
        if (floor == currentFloor) return;
        
        if (floor > currentFloor) {
            upStops.add(floor);
            if (direction == Direction.IDLE) direction = Direction.UP;
        } else {
            downStops.add(floor);
            if (direction == Direction.IDLE) direction = Direction.DOWN;
        }
    }
    
    // Moves the elevator one step and processes stops
    public void move() {
        if (direction == Direction.IDLE) {
            return;
        }

        if (direction == Direction.UP) {
            moveUp();
        } else {
            moveDown();
        }
    }

    private void moveUp() {
        if (upStops.isEmpty() && downStops.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }
        
        // If no more up stops, but we have down stops, switch direction if we are at the highest needed floor
        if (upStops.isEmpty()) {
            if (!downStops.isEmpty()) {
                direction = Direction.DOWN;
                // Don't move yet, just switch
            } else {
                direction = Direction.IDLE;
            }
            return;
        }

        int nextStop = upStops.first();
        if (currentFloor < nextStop) {
            currentFloor++;
            log.info("Elevator {} moved UP to floor {}", id, currentFloor);
        }
        
        if (currentFloor == nextStop) {
            openDoor();
            upStops.remove(nextStop);
            checkDirection();
        }
    }

    private void moveDown() {
         if (upStops.isEmpty() && downStops.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }
        
        if (downStops.isEmpty()) {
             if (!upStops.isEmpty()) {
                direction = Direction.UP;
            } else {
                direction = Direction.IDLE;
            }
            return;
        }

        int nextStop = downStops.first();
        if (currentFloor > nextStop) {
            currentFloor--;
            log.info("Elevator {} moved DOWN to floor {}", id, currentFloor);
        }
        
        if (currentFloor == nextStop) {
            openDoor();
            downStops.remove(nextStop);
            checkDirection();
        }
    }

    private void checkDirection() {
        if (direction == Direction.UP) {
            if (upStops.isEmpty()) {
                if (!downStops.isEmpty()) {
                    direction = Direction.DOWN;
                } else {
                    direction = Direction.IDLE;
                }
            }
        } else if (direction == Direction.DOWN) {
            if (downStops.isEmpty()) {
                if (!upStops.isEmpty()) {
                    direction = Direction.UP;
                } else {
                    direction = Direction.IDLE;
                }
            }
        }
    }

    private void openDoor() {
        log.info("Elevator {} opened door at floor {}", id, currentFloor);
        // Simulate loading/unloading delay if needed
    }
    
    public int getNextStop() {
        if (direction == Direction.UP && !upStops.isEmpty()) return upStops.first();
        if (direction == Direction.DOWN && !downStops.isEmpty()) return downStops.first();
        return -1;
    }
}
