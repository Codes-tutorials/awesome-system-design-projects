package com.example.parkinglot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "parking_spots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    private ParkingSpotType type;

    @Enumerated(EnumType.STRING)
    private SpotStatus status;

    private int floorNumber;
}
