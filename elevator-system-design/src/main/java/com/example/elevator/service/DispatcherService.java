package com.example.elevator.service;

import com.example.elevator.model.Direction;
import com.example.elevator.model.Elevator;
import com.example.elevator.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DispatcherService {

    private final List<Elevator> elevators;

    public DispatcherService() {
        this.elevators = new ArrayList<>();
        // Initialize 3 elevators
        for (int i = 1; i <= 3; i++) {
            elevators.add(new Elevator(i, 10));
        }
    }

    public void handleRequest(Request request) {
        log.info("Received Request: Source {} -> Dest {}", request.getSourceFloor(), request.getDestinationFloor());
        
        Elevator bestElevator = findBestElevator(request);
        
        // Add the pickup stop (source)
        bestElevator.addStop(request.getSourceFloor());
        // We also need to remember to add the destination stop once they are picked up.
        // For simplicity in this simulation, we'll add both stops now, 
        // assuming the passenger presses the button immediately upon entry.
        bestElevator.addStop(request.getDestinationFloor());
        
        log.info("Assigned request to Elevator {}", bestElevator.getId());
    }

    private Elevator findBestElevator(Request request) {
        // Simple strategy: Find closest elevator that is either IDLE or moving in the direction of the request
        // and hasn't passed the source floor yet.
        
        Elevator best = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator e : elevators) {
            int distance = calculateCost(e, request);
            if (distance < minDistance) {
                minDistance = distance;
                best = e;
            }
        }
        return best != null ? best : elevators.get(0); // Fallback
    }

    private int calculateCost(Elevator e, Request req) {
        if (e.getDirection() == Direction.IDLE) {
            return Math.abs(e.getCurrentFloor() - req.getSourceFloor());
        }

        // If moving UP and request is above current floor and request is also UP
        if (e.getDirection() == Direction.UP && req.getSourceFloor() >= e.getCurrentFloor() && req.getDirection() == Direction.UP) {
             return req.getSourceFloor() - e.getCurrentFloor();
        }
        
        // If moving DOWN and request is below current floor and request is also DOWN
        if (e.getDirection() == Direction.DOWN && req.getSourceFloor() <= e.getCurrentFloor() && req.getDirection() == Direction.DOWN) {
            return e.getCurrentFloor() - req.getSourceFloor();
        }

        // Otherwise, it's "away" or "wrong direction". High penalty.
        // In a real system, we'd calculate the distance to the turnaround point.
        // Here, we just add a static penalty.
        return Math.abs(e.getCurrentFloor() - req.getSourceFloor()) + 20;
    }

    public List<Elevator> getElevators() {
        return elevators;
    }
    
    public void step() {
        for (Elevator e : elevators) {
            e.move();
        }
    }
}
