package com.example.parkinglot.web;

import com.example.parkinglot.dto.*;
import com.example.parkinglot.model.Ticket;
import com.example.parkinglot.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<TicketResponse> entry(@RequestBody EntryRequest request) {
        Ticket ticket = parkingService.entry(request.getVehicleNumber(), request.getVehicleType(), request.getGateId());
        
        TicketResponse response = TicketResponse.builder()
                .ticketId(ticket.getId())
                .spotNumber(ticket.getParkingSpot().getSpotNumber())
                .floorNumber(String.valueOf(ticket.getParkingSpot().getFloorNumber()))
                .entryTime(ticket.getEntryTime())
                .status(ticket.getStatus())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit")
    public ResponseEntity<ExitResponse> exit(@RequestBody ExitRequest request) {
        Ticket ticket = parkingService.exit(request.getTicketId(), request.getGateId());

        ExitResponse response = ExitResponse.builder()
                .ticketId(ticket.getId())
                .fee(ticket.getAmount())
                .entryTime(ticket.getEntryTime())
                .exitTime(ticket.getExitTime())
                .status(ticket.getStatus())
                .build();

        return ResponseEntity.ok(response);
    }
}
