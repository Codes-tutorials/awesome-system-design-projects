package com.example.kafka.service;

import com.example.kafka.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Sends a message to the Kafka topic using the userId as the key.
     * Messages with the same key will go to the same partition.
     *
     * @param userId The user ID (used as the partition key)
     * @param message The message content
     */
    public void sendMessage(String userId, String message) {
        log.info("Sending message for user: {}, payload: {}", userId, message);
        
        // Key is crucial here for ordering guarantee per user
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(KafkaConfig.TOPIC_NAME, userId, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[{}] with offset=[{}] to partition=[{}]", 
                        message, result.getRecordMetadata().offset(), result.getRecordMetadata().partition());
            } else {
                log.error("Unable to send message=[{}] due to : {}", message, ex.getMessage());
            }
        });
    }
}
