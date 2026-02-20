package com.microservices.gateway.controller;

import com.microservices.common.dto.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fallback controller for circuit breaker
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/users")
    public ResponseEntity<BaseResponse<String>> userServiceFallback() {
        BaseResponse<String> response = BaseResponse.error(
            "User service is temporarily unavailable. Please try again later.", 
            "SERVICE_UNAVAILABLE"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<BaseResponse<String>> orderServiceFallback() {
        BaseResponse<String> response = BaseResponse.error(
            "Order service is temporarily unavailable. Please try again later.", 
            "SERVICE_UNAVAILABLE"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/inventory")
    public ResponseEntity<BaseResponse<String>> inventoryServiceFallback() {
        BaseResponse<String> response = BaseResponse.error(
            "Inventory service is temporarily unavailable. Please try again later.", 
            "SERVICE_UNAVAILABLE"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/notifications")
    public ResponseEntity<BaseResponse<String>> notificationServiceFallback() {
        BaseResponse<String> response = BaseResponse.error(
            "Notification service is temporarily unavailable. Please try again later.", 
            "SERVICE_UNAVAILABLE"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}