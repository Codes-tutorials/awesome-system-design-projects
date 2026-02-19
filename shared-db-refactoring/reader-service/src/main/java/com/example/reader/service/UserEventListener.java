package com.example.reader.service;

import com.example.reader.model.UserReplica;
import com.example.reader.repository.UserReplicaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserReplicaRepository userReplicaRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-updates", groupId = "reader-service-group")
    public void consumeUserUpdate(String message) {
        log.info("Received User Update: {}", message);
        try {
            UserReplica eventData = objectMapper.readValue(message, UserReplica.class);
            
            // Upsert Logic (Insert or Update)
            eventData.setLastUpdated(LocalDateTime.now());
            userReplicaRepository.save(eventData);
            
            log.info("Updated local replica for User ID: {}", eventData.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse user update", e);
        }
    }
}
