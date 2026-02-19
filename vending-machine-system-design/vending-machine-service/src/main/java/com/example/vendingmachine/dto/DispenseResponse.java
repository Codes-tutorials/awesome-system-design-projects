package com.example.vendingmachine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DispenseResponse {
    private String message;
    private double change;
    private String productName;
}
