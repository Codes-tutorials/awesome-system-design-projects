package com.example.hotel.controller;

import com.example.hotel.model.Amenity;
import com.example.hotel.model.Reservation;
import com.example.hotel.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    public List<Amenity> getAllAmenities() {
        return amenityService.getAllAmenities();
    }

    @PostMapping("/reservations/{reservationId}/add/{amenityId}")
    public ResponseEntity<Reservation> addAmenity(@PathVariable Long reservationId, @PathVariable Long amenityId) {
        return ResponseEntity.ok(amenityService.addAmenityToReservation(reservationId, amenityId));
    }
}
