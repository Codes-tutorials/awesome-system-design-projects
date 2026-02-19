package com.example.elevator;

import com.example.elevator.model.Request;
import com.example.elevator.service.DispatcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ElevatorSystemApplication implements CommandLineRunner {

    private final DispatcherService dispatcherService;

    public static void main(String[] args) {
        SpringApplication.run(ElevatorSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Elevator System Simulation Started. Type 'step' to advance time, 'req <src> <dest>' to add request, or 'exit'.");
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            
            if ("exit".equalsIgnoreCase(line)) break;
            
            if ("step".equalsIgnoreCase(line)) {
                dispatcherService.step();
            } else if (line.startsWith("req")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    try {
                        int src = Integer.parseInt(parts[1]);
                        int dest = Integer.parseInt(parts[2]);
                        dispatcherService.handleRequest(new Request(src, dest));
                    } catch (NumberFormatException e) {
                        log.error("Invalid format. Use: req <src> <dest>");
                    }
                } else {
                    log.error("Invalid format. Use: req <src> <dest>");
                }
            } else {
                log.info("Unknown command.");
            }
            
            // Print status
            dispatcherService.getElevators().forEach(e -> 
                log.info("Elevator {}: Floor {}, Dir {}, Stops {}", e.getId(), e.getCurrentFloor(), e.getDirection(), 
                        e.getDirection() == com.example.elevator.model.Direction.UP ? e.getUpStops() : e.getDownStops())
            );
        }
    }
}
