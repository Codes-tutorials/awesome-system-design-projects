package com.example.hotel.controller;

import com.example.hotel.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/occupancy")
    public Map<String, Object> getOccupancy(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        double occupancyRate = adminService.calculateOccupancyRate(date);
        return Map.of(
            "date", date,
            "occupancyRate", String.format("%.2f%%", occupancyRate)
        );
    }

    @GetMapping("/revenue")
    public Map<String, Object> getRevenue(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Note: Currently returns total value of bookings starting on this date
        BigDecimal revenue = adminService.calculateDailyRevenue(date);
        return Map.of(
            "date", date,
            "totalRevenueBooked", revenue
        );
    }
}
