package com.example.owner.service;

import com.example.owner.model.User;
import com.example.owner.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public User updateUser(Long id, String newName) {
        User user = userRepository.findById(id).orElseThrow();
        user.setName(newName);
        User savedUser = userRepository.save(user);

        // Publish Event (Simulating "Outbox" or CDC)
        publishUserUpdatedEvent(savedUser);
        
        return savedUser;
    }
    
    @Transactional
    public User createUser(User user) {
        User saved = userRepository.save(user);
        publishUserUpdatedEvent(saved);
        return saved;
    }

    private void publishUserUpdatedEvent(User user) {
        try {
            String json = objectMapper.writeValueAsString(user);
            kafkaTemplate.send("user-updates", String.valueOf(user.getId()), json);
            log.info("Published update event for user: {}", user.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user event", e);
        }
    }
}
