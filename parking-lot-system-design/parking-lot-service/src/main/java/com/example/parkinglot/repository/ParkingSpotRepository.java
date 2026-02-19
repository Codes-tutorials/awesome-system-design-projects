package com.example.parkinglot.repository;

import com.example.parkinglot.model.ParkingSpot;
import com.example.parkinglot.model.ParkingSpotType;
import com.example.parkinglot.model.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    Optional<ParkingSpot> findFirstByTypeAndStatus(ParkingSpotType type, SpotStatus status);
    List<ParkingSpot> findByStatus(SpotStatus status);
}
