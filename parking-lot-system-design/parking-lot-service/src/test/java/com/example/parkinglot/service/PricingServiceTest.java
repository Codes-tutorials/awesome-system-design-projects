package com.example.parkinglot.service;

import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }

    @Test
    void calculateFee_Car_1Hour() {
        Ticket ticket = new Ticket();
        ticket.setVehicleType(VehicleType.CAR);
        ticket.setEntryTime(LocalDateTime.now().minusHours(1));
        ticket.setExitTime(LocalDateTime.now());

        double fee = pricingService.calculateFee(ticket);
        assertEquals(10.0, fee);
    }

    @Test
    void calculateFee_Bike_2Hours() {
        Ticket ticket = new Ticket();
        ticket.setVehicleType(VehicleType.BIKE);
        ticket.setEntryTime(LocalDateTime.now().minusHours(2));
        ticket.setExitTime(LocalDateTime.now());

        double fee = pricingService.calculateFee(ticket);
        assertEquals(10.0, fee); // 5 * 2
    }

    @Test
    void calculateFee_Minimum1Hour() {
        Ticket ticket = new Ticket();
        ticket.setVehicleType(VehicleType.CAR);
        ticket.setEntryTime(LocalDateTime.now().minusMinutes(30));
        ticket.setExitTime(LocalDateTime.now());

        double fee = pricingService.calculateFee(ticket);
        assertEquals(10.0, fee);
    }
}
