package com.example.hotel.dto;

import com.example.hotel.model.Guest;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    private Long roomId;
    private Guest guest;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
