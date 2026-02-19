package com.example.parkinglot.service;

import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.model.VehicleType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {

    private static final double HOURLY_RATE_CAR = 10.0;
    private static final double HOURLY_RATE_BIKE = 5.0;
    private static final double HOURLY_RATE_TRUCK = 20.0;

    public double calculateFee(Ticket ticket) {
        if (ticket.getEntryTime() == null) {
            throw new IllegalArgumentException("Entry time cannot be null");
        }

        LocalDateTime exitTime = ticket.getExitTime() != null ? ticket.getExitTime() : LocalDateTime.now();
        long hours = Duration.between(ticket.getEntryTime(), exitTime).toHours();
        if (hours == 0) hours = 1; // Minimum 1 hour

        double rate = switch (ticket.getVehicleType()) {
            case CAR -> HOURLY_RATE_CAR;
            case BIKE -> HOURLY_RATE_BIKE;
            case TRUCK -> HOURLY_RATE_TRUCK;
        };

        return hours * rate;
    }
}
