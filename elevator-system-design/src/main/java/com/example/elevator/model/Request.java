package com.example.elevator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Request {
    private int sourceFloor;
    private int destinationFloor;
    
    public Direction getDirection() {
        if (destinationFloor > sourceFloor) return Direction.UP;
        if (destinationFloor < sourceFloor) return Direction.DOWN;
        return Direction.IDLE;
    }
}
