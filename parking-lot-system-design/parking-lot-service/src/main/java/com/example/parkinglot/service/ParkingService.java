package com.example.parkinglot.service;

import com.example.parkinglot.model.*;
import com.example.parkinglot.repository.ParkingSpotRepository;
import com.example.parkinglot.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final TicketRepository ticketRepository;
    private final PricingService pricingService;

    @Transactional
    public Ticket entry(String vehicleNumber, VehicleType vehicleType, String gateId) {
        ParkingSpotType spotType = getSpotTypeForVehicle(vehicleType);
        
        // Simple strategy: First available spot
        ParkingSpot spot = parkingSpotRepository.findFirstByTypeAndStatus(spotType, SpotStatus.FREE)
                .orElseThrow(() -> new RuntimeException("Parking Full for type: " + vehicleType));

        spot.setStatus(SpotStatus.OCCUPIED);
        parkingSpotRepository.save(spot);

        Ticket ticket = new Ticket();
        ticket.setVehicleNumber(vehicleNumber);
        ticket.setVehicleType(vehicleType);
        ticket.setParkingSpot(spot);
        ticket.setEntryTime(LocalDateTime.now());
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setGateId(gateId);

        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket exit(String ticketId, String gateId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.ACTIVE) {
            throw new RuntimeException("Ticket is not active");
        }

        ticket.setExitTime(LocalDateTime.now());
        double fee = pricingService.calculateFee(ticket);
        ticket.setAmount(fee);
        ticket.setStatus(TicketStatus.PAID); // Simulating immediate payment for simplicity

        ParkingSpot spot = ticket.getParkingSpot();
        spot.setStatus(SpotStatus.FREE);
        parkingSpotRepository.save(spot);

        return ticketRepository.save(ticket);
    }

    private ParkingSpotType getSpotTypeForVehicle(VehicleType vehicleType) {
        return switch (vehicleType) {
            case CAR -> ParkingSpotType.COMPACT;
            case BIKE -> ParkingSpotType.BIKE;
            case TRUCK -> ParkingSpotType.LARGE;
        };
    }
}
