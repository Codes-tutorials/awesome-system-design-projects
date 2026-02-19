package com.example.logging.controller;

import com.example.logging.model.LogEntry;
import com.example.logging.service.IndexService;
import com.example.logging.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final IngestionService ingestionService;
    private final IndexService indexService;

    public LogController(IngestionService ingestionService, IndexService indexService) {
        this.ingestionService = ingestionService;
        this.indexService = indexService;
    }

    @PostMapping
    public ResponseEntity<String> ingestLog(@RequestBody LogEntry logEntry) {
        ingestionService.ingestLog(logEntry);
        return ResponseEntity.accepted().body("Log queued for processing");
    }

    @GetMapping
    public ResponseEntity<List<LogEntry>> searchLogs(@RequestParam("q") String query) {
        List<LogEntry> results = indexService.search(query);
        return ResponseEntity.ok(results);
    }
}
