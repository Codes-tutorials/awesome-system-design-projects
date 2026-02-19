package com.example.parkinglot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private ParkingSpot parkingSpot;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private double amount;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private String gateId;
}
