package com.example.vendingmachine.service;

import lombok.Data;

@Data
public class UserSession {
    private String sessionId;
    private Long selectedProductId;
    private double currentBalance;
    
    public UserSession(String sessionId) {
        this.sessionId = sessionId;
        this.currentBalance = 0.0;
    }
}
