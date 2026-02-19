package com.example.hotel.controller;

import com.example.hotel.dto.BookingRequest;
import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;
import com.example.hotel.model.RoomType;
import com.example.hotel.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/search")
    public ResponseEntity<List<Room>> findAvailableRooms(
            @RequestParam Long hotelId,
            @RequestParam RoomType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        
        return ResponseEntity.ok(bookingService.findAvailableRooms(hotelId, type, checkIn, checkOut));
    }

    @PostMapping
    public ResponseEntity<?> bookRoom(@RequestBody BookingRequest request) {
        try {
            Reservation reservation = bookingService.bookRoom(
                    request.getRoomId(),
                    request.getGuest(),
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );
            return ResponseEntity.ok(reservation);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}
