package com.example.logging.service;

import com.example.logging.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class StorageService {

    private final String BASE_DIR = "logs_storage";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

    public StorageService() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    /**
     * Persists the log entry to a daily file.
     * Returns the file path and line number/offset (simplified as just path for now).
     */
    public synchronized String persist(LogEntry logEntry) {
        if (logEntry.getId() == null) {
            logEntry.setId(UUID.randomUUID().toString());
        }

        String dateStr = dateFormatter.format(logEntry.getTimestamp());
        Path filePath = Paths.get(BASE_DIR, dateStr + ".log");

        try {
            String json = objectMapper.writeValueAsString(logEntry);
            Files.writeString(filePath, json + System.lineSeparator(), 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return filePath.toString();
        } catch (IOException e) {
            e.printStackTrace(); // In real app, handle error/retry
            return null;
        }
    }
}
