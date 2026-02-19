package com.example.hotel.service;

import com.example.hotel.model.*;
import com.example.hotel.repository.GuestRepository;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final GuestRepository guestRepository;
    private final PricingService pricingService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    @Cacheable(value = "availableRooms", key = "{#hotelId, #type, #checkIn, #checkOut}")
    public List<Room> findAvailableRooms(Long hotelId, RoomType type, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailableRooms(hotelId, type, checkIn, checkOut);
    }

    @Transactional
    @CacheEvict(value = "availableRooms", allEntries = true)
    public Reservation bookRoom(Long roomId, Guest guestInfo, LocalDate checkIn, LocalDate checkOut) {
        // 1. Lock the room to prevent concurrent bookings
        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // 2. Double-check availability (crucial after acquiring lock)
        List<Reservation> conflicts = reservationRepository.findOverlappingReservations(roomId, checkIn, checkOut);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Room is already booked for the selected dates");
        }

        // 3. Find or Create Guest
        Guest guest = guestRepository.findByEmail(guestInfo.getEmail())
                .orElseGet(() -> guestRepository.save(guestInfo));

        // 4. Calculate Price
        java.math.BigDecimal price = pricingService.calculateTotalPrice(room, checkIn, checkOut);

        // 5. Create Reservation
        Reservation reservation = Reservation.builder()
                .room(room)
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(ReservationStatus.PAYMENT_PENDING) // Start as PENDING
                .totalPrice(price)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 6. Publish Event (Async Notification)
        // Note: Ideally, we should publish this only after Payment Success. 
        // For now, we'll keep it here or move it to PaymentService.
        // Let's comment it out here and assume PaymentService handles confirmation email.
        // eventPublisher.publishEvent(new BookingCreatedEvent(this, savedReservation));

        return savedReservation;
    }
}
