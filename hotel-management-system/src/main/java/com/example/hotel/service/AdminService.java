package com.example.hotel.service;

import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public double calculateOccupancyRate(LocalDate date) {
        long totalRooms = roomRepository.count();
        if (totalRooms == 0) return 0.0;
        
        long occupiedRooms = reservationRepository.countReservationsByDate(date);
        return (double) occupiedRooms / totalRooms * 100;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDailyRevenue(LocalDate date) {
        // In a real app, you'd calculate pro-rated revenue.
        // For simplicity, we'll sum up totalPrice of all reservations active on this date, 
        // divided by their duration (simplified approach) OR just sum bookings made on that day.
        // Let's implement: Sum of (totalPrice / stayDuration) for all active reservations on 'date'.
        // BUT, since that logic is complex in SQL/Java, let's simplify: 
        // "Revenue realized on this date" = sum of room prices for that night.
        // We'll rely on the repository to sum up room prices for reservations covering this date.
        
        return reservationRepository.calculateRevenueForDate(date);
    }
}
