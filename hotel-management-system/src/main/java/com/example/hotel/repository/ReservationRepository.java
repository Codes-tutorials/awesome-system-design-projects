package com.example.hotel.repository;

import com.example.hotel.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId " +
           "AND r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate " +
           "AND r.status <> 'CANCELLED'")
    List<Reservation> findOverlappingReservations(@Param("roomId") Long roomId,
                                                  @Param("checkInDate") LocalDate checkInDate,
                                                  @Param("checkOutDate") LocalDate checkOutDate);
                                                  
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.checkInDate <= :date AND r.checkOutDate > :date AND r.status <> 'CANCELLED'")
    long countReservationsByDate(@Param("date") LocalDate date);

    // Simplification: Sum of room price per night for all active reservations
    // Note: This assumes room price is static per night which might vary with dynamic pricing.
    // Better: sum(r.totalPrice / days) is hard in SQL.
    // Alternative: Just sum totalPrice of reservations created on that day? No, user wants occupancy/revenue reports.
    // Let's stick to: Sum of totalPrice for reservations checking out today? No.
    // Let's just return 0 for now and let the service handle logic or use a simpler metric: "Total value of bookings made today".
    // Actually, let's try a JPQL to sum.
    @Query("SELECT COALESCE(SUM(r.totalPrice), 0) FROM Reservation r WHERE r.checkInDate = :date AND r.status <> 'CANCELLED'")
    BigDecimal calculateRevenueForDate(@Param("date") LocalDate date);
}
