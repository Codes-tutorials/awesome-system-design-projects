package com.example.parkinglot.dto;

import com.example.parkinglot.model.VehicleType;
import lombok.Data;

@Data
public class EntryRequest {
    private String vehicleNumber;
    private VehicleType vehicleType;
    private String gateId;
}
