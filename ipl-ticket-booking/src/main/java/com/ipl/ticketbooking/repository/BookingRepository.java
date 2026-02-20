package com.ipl.ticketbooking.repository;

import com.ipl.ticketbooking.model.Booking;
import com.ipl.ticketbooking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Finds booking by ID and user ID for security
     */
    Optional<Booking> findByIdAndUserId(Long id, Long userId);
    
    /**
     * Finds booking by reference number
     */
    Optional<Booking> findByBookingReference(String bookingReference);
    
    /**
     * Finds user's bookings with pagination
     */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Finds bookings by match
     */
    List<Booking> findByMatchId(Long matchId);
    
    /**
     * Finds bookings by status
     */
    List<Booking> findByStatus(BookingStatus status);
    
    /**
     * Finds pending bookings older than specified time (for cleanup)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :cutoffTime")
    List<Booking> findExpiredPendingBookings(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Gets booking statistics for a match
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN b.status = 'CONFIRMED' THEN 1 END) as confirmed, " +
           "COUNT(CASE WHEN b.status = 'PENDING' THEN 1 END) as pending, " +
           "COUNT(CASE WHEN b.status = 'CANCELLED' THEN 1 END) as cancelled, " +
           "SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.totalAmount ELSE 0 END) as totalRevenue, " +
           "SUM(CASE WHEN b.status = 'CONFIRMED' THEN b.totalSeats ELSE 0 END) as totalSeatsBooked " +
           "FROM Booking b WHERE b.match.id = :matchId")
    Object[] getBookingStatistics(@Param("matchId") Long matchId);
    
    /**
     * Gets daily booking statistics
     */
    @Query("SELECT DATE(b.createdAt) as bookingDate, " +
           "COUNT(b) as totalBookings, " +
           "SUM(b.totalAmount) as totalRevenue, " +
           "SUM(b.totalSeats) as totalSeats " +
           "FROM Booking b WHERE b.createdAt BETWEEN :startDate AND :endDate " +
           "AND b.status = 'CONFIRMED' " +
           "GROUP BY DATE(b.createdAt) " +
           "ORDER BY DATE(b.createdAt)")
    List<Object[]> getDailyBookingStatistics(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds user's recent bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
           "AND b.createdAt >= :since " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookingsByUser(@Param("userId") Long userId, 
                                          @Param("since") LocalDateTime since);
    
    /**
     * Counts user's bookings for a specific match (to prevent multiple bookings)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId " +
           "AND b.match.id = :matchId " +
           "AND b.status IN ('PENDING', 'CONFIRMED')")
    long countActiveBookingsByUserAndMatch(@Param("userId") Long userId, 
                                          @Param("matchId") Long matchId);
    
    /**
     * Finds top users by booking volume
     */
    @Query("SELECT b.user.id, b.user.firstName, b.user.lastName, " +
           "COUNT(b) as bookingCount, SUM(b.totalAmount) as totalSpent " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.createdAt >= :since " +
           "GROUP BY b.user.id, b.user.firstName, b.user.lastName " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findTopUsersByBookingVolume(@Param("since") LocalDateTime since, 
                                              Pageable pageable);
}