package com.example.largetable.controller;

import com.example.largetable.service.BatchUpdateService;
import com.example.largetable.service.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final DataSeeder dataSeeder;
    private final BatchUpdateService batchUpdateService;

    @PostMapping("/seed")
    public ResponseEntity<String> seedData(@RequestParam(defaultValue = "100000") int count) {
        CompletableFuture.runAsync(() -> dataSeeder.seedData(count));
        return ResponseEntity.ok("Data seeding started for " + count + " records.");
    }

    @PostMapping("/update-status")
    public ResponseEntity<String> updateStatus(
            @RequestParam String oldStatus, 
            @RequestParam String newStatus,
            @RequestParam(defaultValue = "1000") int batchSize) {
        
        CompletableFuture.runAsync(() -> 
            batchUpdateService.updateStatusEfficiently(oldStatus, newStatus, batchSize)
        );
        
        return ResponseEntity.ok("Batch update job started.");
    }
}
