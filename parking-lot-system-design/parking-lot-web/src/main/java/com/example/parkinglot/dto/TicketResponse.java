package com.example.parkinglot.dto;

import com.example.parkinglot.model.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private String ticketId;
    private String spotNumber;
    private String floorNumber;
    private LocalDateTime entryTime;
    private TicketStatus status;
}
