package com.ipl.ticketbooking.repository;

import com.ipl.ticketbooking.model.Seat;
import com.ipl.ticketbooking.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    /**
     * CRITICAL QUERY: Finds seats with pessimistic locking for booking
     * Prevents race conditions during concurrent booking attempts
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds AND s.match.id = :matchId")
    List<Seat> findByIdInAndMatchIdForUpdate(@Param("seatIds") List<Long> seatIds, 
                                            @Param("matchId") Long matchId);
    
    /**
     * Finds available seats for a match with category filtering
     */
    @Query("SELECT s FROM Seat s WHERE s.match.id = :matchId AND s.status = :status " +
           "AND (:categoryId IS NULL OR s.seatCategory.id = :categoryId) " +
           "ORDER BY s.seatCategory.priceMultiplier, s.rowNumber, s.seatInRow")
    List<Seat> findAvailableSeats(@Param("matchId") Long matchId, 
                                 @Param("status") SeatStatus status,
                                 @Param("categoryId") Long categoryId);
    
    /**
     * Counts available seats by category for a match
     */
    @Query("SELECT s.seatCategory.id, s.seatCategory.categoryName, COUNT(s) " +
           "FROM Seat s WHERE s.match.id = :matchId AND s.status = 'AVAILABLE' " +
           "GROUP BY s.seatCategory.id, s.seatCategory.categoryName")
    List<Object[]> countAvailableSeatsByCategory(@Param("matchId") Long matchId);
    
    /**
     * Finds seats that are locked but expired
     */
    @Query("SELECT s FROM Seat s WHERE s.lockedUntil IS NOT NULL AND s.lockedUntil < :currentTime")
    List<Seat> findExpiredLockedSeats(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Finds seats locked by a specific user
     */
    @Query("SELECT s FROM Seat s WHERE s.lockedByUser = :userId AND s.lockedUntil > :currentTime")
    List<Seat> findSeatsLockedByUser(@Param("userId") Long userId, 
                                    @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Bulk update to release expired locks
     */
    @Query("UPDATE Seat s SET s.lockedByUser = NULL, s.lockedUntil = NULL " +
           "WHERE s.lockedUntil IS NOT NULL AND s.lockedUntil < :currentTime")
    int releaseExpiredLocks(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Finds seats by match and section for seat map display
     */
    @Query("SELECT s FROM Seat s WHERE s.match.id = :matchId " +
           "AND s.seatCategory.sectionCode = :sectionCode " +
           "ORDER BY s.rowNumber, s.seatInRow")
    List<Seat> findByMatchAndSection(@Param("matchId") Long matchId, 
                                    @Param("sectionCode") String sectionCode);
    
    /**
     * Gets seat statistics for a match
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN s.status = 'AVAILABLE' THEN 1 END) as available, " +
           "COUNT(CASE WHEN s.status = 'BOOKED' THEN 1 END) as booked, " +
           "COUNT(CASE WHEN s.status = 'BLOCKED' THEN 1 END) as blocked, " +
           "COUNT(s) as total " +
           "FROM Seat s WHERE s.match.id = :matchId")
    Object[] getSeatStatistics(@Param("matchId") Long matchId);
    
    /**
     * Finds best available seats (closest to front, center)
     */
    @Query("SELECT s FROM Seat s WHERE s.match.id = :matchId AND s.status = 'AVAILABLE' " +
           "AND s.seatCategory.id = :categoryId " +
           "ORDER BY s.rowNumber ASC, ABS(s.seatInRow - :centerSeat) ASC")
    List<Seat> findBestAvailableSeats(@Param("matchId") Long matchId, 
                                     @Param("categoryId") Long categoryId,
                                     @Param("centerSeat") Integer centerSeat);
}