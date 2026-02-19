package com.example.ratelimiter.controller;

import com.example.ratelimiter.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    // Requirement 1: Limit requests per user (e.g., 100 requests/min -> 1.67 req/sec)
    // We'll set a smaller limit for demo: 5 requests per 10 seconds (0.5 req/sec, burst 5)
    @GetMapping("/user-limited")
    @RateLimit(key = "user_action", rate = 0.5, capacity = 5)
    public String userLimited() {
        return "Request allowed for user!";
    }

    // Requirement 2: Limit requests globally (e.g., 1M requests/sec)
    // Demo: 10 requests per second globally, burst 20
    @GetMapping("/global-limited")
    @RateLimit(key = "global_api", rate = 10.0, capacity = 20, isUserSpecific = false)
    public String globalLimited() {
        return "Global request allowed!";
    }
    
    // Test burst: Low rate (1 per 5 sec) but high burst (10)
    @GetMapping("/burst-test")
    @RateLimit(key = "burst_test", rate = 0.2, capacity = 10)
    public String burstTest() {
        return "Burst request allowed!";
    }
}
