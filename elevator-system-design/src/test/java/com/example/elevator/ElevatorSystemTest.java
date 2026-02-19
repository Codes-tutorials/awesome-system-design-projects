package com.example.elevator;

import com.example.elevator.model.Direction;
import com.example.elevator.model.Elevator;
import com.example.elevator.model.Request;
import com.example.elevator.service.DispatcherService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElevatorSystemTest {

    @Test
    void testSingleElevatorLogic() {
        Elevator elevator = new Elevator(1, 10);
        
        // Request: 0 -> 5
        elevator.addStop(5);
        
        assertEquals(Direction.UP, elevator.getDirection());
        
        // Move up
        for (int i = 0; i < 5; i++) elevator.move();
        
        assertEquals(5, elevator.getCurrentFloor());
        assertEquals(Direction.IDLE, elevator.getDirection());
    }

    @Test
    void testLookAlgorithm() {
        Elevator elevator = new Elevator(1, 10);
        
        // Current: 0. Stops: 5, 3.
        elevator.addStop(5);
        elevator.addStop(3);
        
        elevator.move(); // 1
        elevator.move(); // 2
        elevator.move(); // 3 - Should stop/open door here first
        
        assertEquals(3, elevator.getCurrentFloor());
        assertTrue(elevator.getUpStops().contains(5));
        
        elevator.move(); // 3 (Door open) -> 3 (Wait?) -> Actually logic moves immediately if door closed.
        // My simple logic: openDoor is instantaneous in 'move'.
        
        elevator.move(); // 4
        elevator.move(); // 5
        
        assertEquals(5, elevator.getCurrentFloor());
        assertEquals(Direction.IDLE, elevator.getDirection());
    }

    @Test
    void testDispatcherSelection() {
        DispatcherService dispatcher = new DispatcherService();
        
        // E1 at 0, E2 at 0, E3 at 0
        
        // Move E1 to 10 manually
        dispatcher.getElevators().get(0).addStop(10);
        for(int i=0; i<10; i++) dispatcher.getElevators().get(0).move();
        
        // E1 is at 10 (IDLE). E2 is at 0 (IDLE).
        
        // Request from 2 -> 5. E2 is closer (dist 2) than E1 (dist 8).
        dispatcher.handleRequest(new Request(2, 5));
        
        // Check if E2 got it (Elevator IDs are 1-based in my service)
        Elevator e2 = dispatcher.getElevators().get(1); 
        assertTrue(e2.getUpStops().contains(2));
    }
}
