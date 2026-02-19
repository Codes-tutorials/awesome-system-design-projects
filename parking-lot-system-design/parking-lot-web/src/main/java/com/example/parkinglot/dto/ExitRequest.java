package com.example.parkinglot.dto;

import lombok.Data;

@Data
public class ExitRequest {
    private String ticketId;
    private String gateId;
}
