package com.example.hotel.controller;

import com.example.hotel.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        Long reservationId = Long.valueOf(payload.get("reservationId").toString());
        boolean success = Boolean.TRUE.equals(payload.get("success"));

        paymentService.processPaymentWebhook(reservationId, success);
        return ResponseEntity.ok("Webhook processed");
    }
}
