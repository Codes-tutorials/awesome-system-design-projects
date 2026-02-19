package com.example.logging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private String id;
    private Instant timestamp;
    private String service;
    private String level;
    private String message;
    private Map<String, String> metadata;
}
