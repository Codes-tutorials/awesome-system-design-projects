package com.example.logging.service;

import com.example.logging.model.LogEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class IngestionService {

    private final StorageService storageService;
    private final IndexService indexService;
    
    // Simulating a Message Queue (e.g., Kafka)
    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();

    public IngestionService(StorageService storageService, IndexService indexService) {
        this.storageService = storageService;
        this.indexService = indexService;
    }

    public void ingestLog(LogEntry logEntry) {
        // Enrichment
        if (logEntry.getTimestamp() == null) {
            logEntry.setTimestamp(Instant.now());
        }
        if (logEntry.getId() == null) {
            logEntry.setId(UUID.randomUUID().toString());
        }

        // Push to Queue (Non-blocking or blocking depending on requirements)
        logQueue.offer(logEntry);
    }

    @PostConstruct
    public void startProcessor() {
        // Start a background thread to process logs from the queue
        Thread processorThread = new Thread(this::processLogs);
        processorThread.setName("LogProcessor-Thread");
        processorThread.start();
    }

    private void processLogs() {
        while (true) {
            try {
                LogEntry entry = logQueue.take(); // Block until log is available
                
                // 1. Persist to Storage (Cold Storage / File System)
                storageService.persist(entry);
                
                // 2. Index for Search (Hot Storage / Elasticsearch)
                indexService.index(entry);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace(); // Handle processing errors
            }
        }
    }
}
