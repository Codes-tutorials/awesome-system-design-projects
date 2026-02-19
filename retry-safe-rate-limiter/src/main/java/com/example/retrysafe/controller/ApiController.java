package com.example.retrysafe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final Random random = new Random();

    @GetMapping("/resource")
    public ResponseEntity<String> getResource(@RequestParam(defaultValue = "false") boolean fail) {
        if (fail) {
            // Simulate 500 error
            return ResponseEntity.internalServerError().body("Simulated Server Failure");
        }
        return ResponseEntity.ok("Success");
    }
    
    @GetMapping("/flaky")
    public ResponseEntity<String> flakyResource() {
        if (random.nextInt(10) < 7) { // 70% failure rate
             return ResponseEntity.internalServerError().body("Random Flaky Failure");
        }
        return ResponseEntity.ok("Success");
    }
}
