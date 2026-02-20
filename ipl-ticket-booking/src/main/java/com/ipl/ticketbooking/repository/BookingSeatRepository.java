package com.ipl.ticketbooking.repository;

import com.ipl.ticketbooking.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    
    /**
     * Finds all seats for a booking
     */
    List<BookingSeat> findByBookingId(Long bookingId);
    
    /**
     * Finds booking seat by seat ID
     */
    BookingSeat findBySeatId(Long seatId);
    
    /**
     * Gets seat details for a booking with category information
     */
    @Query("SELECT bs, s.seatCategory FROM BookingSeat bs " +
           "JOIN bs.seat s " +
           "WHERE bs.booking.id = :bookingId " +
           "ORDER BY s.rowNumber, s.seatInRow")
    List<Object[]> findBookingSeatDetailsWithCategory(@Param("bookingId") Long bookingId);
}