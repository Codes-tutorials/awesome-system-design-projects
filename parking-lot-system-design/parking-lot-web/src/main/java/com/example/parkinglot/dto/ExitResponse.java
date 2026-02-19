package com.example.parkinglot.dto;

import com.example.parkinglot.model.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExitResponse {
    private String ticketId;
    private double fee;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private TicketStatus status;
}
