package com.example.kafka.controller;

import com.example.kafka.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final KafkaProducerService producerService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEvent(@RequestParam String userId, @RequestParam String message) {
        producerService.sendMessage(userId, message);
        return ResponseEntity.ok("Message sent successfully");
    }

    /**
     * Sends a bulk of messages to demonstrate batching.
     * 
     * @param userId The user ID (key)
     * @param count Number of messages to send
     * @return Response string
     */
    @PostMapping("/send-bulk")
    public ResponseEntity<String> sendBulkEvents(@RequestParam String userId, @RequestParam(defaultValue = "100") int count) {
        long startTime = System.currentTimeMillis();
        
        IntStream.range(0, count).forEach(i -> {
            producerService.sendMessage(userId, "Bulk message " + i + " for " + userId);
        });
        
        long duration = System.currentTimeMillis() - startTime;
        return ResponseEntity.ok("Triggered " + count + " messages for user " + userId + " in " + duration + "ms");
    }
}
