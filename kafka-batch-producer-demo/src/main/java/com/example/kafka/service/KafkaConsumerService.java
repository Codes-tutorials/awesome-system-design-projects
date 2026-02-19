package com.example.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Batch listener that receives a list of messages.
     * Supports partial batch failure handling via BatchListenerFailedException.
     */
    @KafkaListener(topics = "user-events", groupId = "batch-demo-group")
    public void listen(@Payload List<String> messages,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                       @Header(KafkaHeaders.OFFSET) List<Long> offsets,
                       @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys) {
        
        log.info("- - - - - - - - - - - - - - - - - - - - - - - - -");
        log.info("Received batch of {} messages", messages.size());
        
        for (int i = 0; i < messages.size(); i++) {
            try {
                processMessage(keys.get(i), messages.get(i), partitions.get(i), offsets.get(i));
            } catch (Exception e) {
                log.error("Error processing message at index {}: {}", i, messages.get(i));
                // Throwing this exception tells Spring which record failed.
                // Spring will commit offsets before this index and retry from this index.
                throw new BatchListenerFailedException("Failed to process message", e, i);
            }
        }
        log.info("- - - - - - - - - - - - - - - - - - - - - - - - -");
    }

    private void processMessage(String key, String message, int partition, long offset) {
        log.info("Processing message: key={}, partition={}, offset={}, payload={}", 
                key, partition, offset, message);
        
        // Simulate a "poison pill" or business logic failure
        if ("fail".equals(message)) {
            throw new RuntimeException("Simulated business logic failure");
        }
    }
}
