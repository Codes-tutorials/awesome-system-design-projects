package com.example.logging.component;

import com.example.logging.model.LogEntry;
import com.example.logging.service.IngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LogGenerator implements CommandLineRunner {

    private final IngestionService ingestionService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();

    public LogGenerator(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting Log Generator Simulation...");
        
        // Generate a log every 2 seconds
        scheduler.scheduleAtFixedRate(this::generateRandomLog, 0, 2, TimeUnit.SECONDS);
    }

    private void generateRandomLog() {
        String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};
        String[] services = {"order-service", "payment-service", "user-service", "inventory-service"};
        String[] messages = {
            "Database connection established",
            "Payment processed successfully",
            "User not found",
            "Inventory updated",
            "NullPointerException in handler",
            "Timeout waiting for upstream"
        };

        LogEntry entry = LogEntry.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .level(levels[random.nextInt(levels.length)])
                .service(services[random.nextInt(services.length)])
                .message(messages[random.nextInt(messages.length)])
                .metadata(Map.of("traceId", UUID.randomUUID().toString().substring(0, 8)))
                .build();

        ingestionService.ingestLog(entry);
        System.out.println("Generated Log: [" + entry.getLevel() + "] " + entry.getMessage());
    }
}
