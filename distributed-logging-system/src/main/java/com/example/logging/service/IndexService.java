package com.example.logging.service;

import com.example.logging.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class IndexService {

    // Inverted Index: Keyword -> List of LogEntries
    // In a real system, this would be Lucene/Elasticsearch storing Document IDs
    private final Map<String, List<LogEntry>> invertedIndex = new ConcurrentHashMap<>();

    public void index(LogEntry logEntry) {
        // Tokenize the message and other fields
        Set<String> tokens = tokenize(logEntry);

        for (String token : tokens) {
            invertedIndex.computeIfAbsent(token, k -> Collections.synchronizedList(new ArrayList<>()))
                         .add(logEntry);
        }
    }

    public List<LogEntry> search(String query) {
        String key = query.toLowerCase().trim();
        return invertedIndex.getOrDefault(key, Collections.emptyList());
    }

    private Set<String> tokenize(LogEntry logEntry) {
        Set<String> tokens = new HashSet<>();
        
        // Index Level
        if (logEntry.getLevel() != null) {
            tokens.add(logEntry.getLevel().toLowerCase());
        }
        
        // Index Service Name
        if (logEntry.getService() != null) {
            tokens.add(logEntry.getService().toLowerCase());
        }

        // Index Message Words
        if (logEntry.getMessage() != null) {
            String[] words = logEntry.getMessage().toLowerCase().split("\\s+");
            tokens.addAll(Arrays.asList(words));
        }

        return tokens;
    }
}
