package com.example.hotel.service;

import com.example.hotel.model.Amenity;
import com.example.hotel.model.Reservation;
import com.example.hotel.repository.AmenityRepository;
import com.example.hotel.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;
    private final ReservationRepository reservationRepository;

    public List<Amenity> getAllAmenities() {
        return amenityRepository.findAll();
    }

    @Transactional
    public Reservation addAmenityToReservation(Long reservationId, Long amenityId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new IllegalArgumentException("Amenity not found"));

        reservation.getAmenities().add(amenity);
        
        // Update total price
        reservation.setTotalPrice(reservation.getTotalPrice().add(amenity.getPrice()));
        
        return reservationRepository.save(reservation);
    }
}
