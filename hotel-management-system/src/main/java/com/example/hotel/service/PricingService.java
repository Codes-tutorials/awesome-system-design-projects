package com.example.hotel.service;

import com.example.hotel.model.Room;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PricingService {

    public BigDecimal calculateTotalPrice(Room room, LocalDate checkIn, LocalDate checkOut) {
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) return BigDecimal.ZERO;

        BigDecimal basePrice = room.getPricePerNight();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // Simple dynamic pricing: Weekends are 20% more expensive
        for (int i = 0; i < days; i++) {
            LocalDate date = checkIn.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();
            
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                totalPrice = totalPrice.add(basePrice.multiply(new BigDecimal("1.20")));
            } else {
                totalPrice = totalPrice.add(basePrice);
            }
        }
        
        return totalPrice;
    }
}
