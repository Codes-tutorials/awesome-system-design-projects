package com.example.hotel.repository;

import com.example.hotel.model.Room;
import com.example.hotel.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") Long id);
    
    // Find rooms that are NOT booked in the given date range
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.type = :type AND r.id NOT IN " +
           "(SELECT res.room.id FROM Reservation res WHERE " +
           "(res.checkInDate < :checkOutDate AND res.checkOutDate > :checkInDate) " +
           "AND res.status <> 'CANCELLED')")
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId, 
                                  @Param("type") RoomType type,
                                  @Param("checkInDate") LocalDate checkInDate, 
                                  @Param("checkOutDate") LocalDate checkOutDate);
}
