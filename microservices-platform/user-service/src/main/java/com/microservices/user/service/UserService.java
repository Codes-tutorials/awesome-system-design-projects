package com.microservices.user.service;

import com.microservices.user.dto.CreateUserRequest;
import com.microservices.user.dto.UpdateUserRequest;
import com.microservices.user.dto.UserResponse;
import com.microservices.user.events.UserCreatedEvent;
import com.microservices.user.events.UserUpdatedEvent;
import com.microservices.user.exception.UserAlreadyExistsException;
import com.microservices.user.exception.UserNotFoundException;
import com.microservices.user.model.User;
import com.microservices.user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_EVENTS_TOPIC = "user-events";
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @CircuitBreaker(name = "user-service", fallbackMethod = "createUserFallback")
    @Retry(name = "user-service")
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating user with username: {}", request.getUsername());
        
        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }
        
        // Create user
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName()
        );
        user.setPhoneNumber(request.getPhoneNumber());
        
        User savedUser = userRepository.save(user);
        
        // Publish event
        UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName()
        );
        
        kafkaTemplate.send(USER_EVENTS_TOPIC, event);
        logger.info("User created successfully with ID: {}", savedUser.getId());
        
        return mapToUserResponse(savedUser);
    }
    
    public UserResponse getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        return mapToUserResponse(user);
    }
    
    public UserResponse getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        
        return mapToUserResponse(user);
    }
    
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        
        return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    @CircuitBreaker(name = "user-service", fallbackMethod = "updateUserFallback")
    @Retry(name = "user-service")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        
        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        User updatedUser = userRepository.save(user);
        
        // Publish event
        UserUpdatedEvent event = new UserUpdatedEvent(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getFirstName(),
            updatedUser.getLastName(),
            updatedUser.getStatus().toString()
        );
        
        kafkaTemplate.send(USER_EVENTS_TOPIC, event);
        logger.info("User updated successfully with ID: {}", updatedUser.getId());
        
        return mapToUserResponse(updatedUser);
    }
    
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }
    
    public List<UserResponse> searchUsersByName(String name) {
        logger.info("Searching users by name: {}", name);
        
        return userRepository.findByNameContaining(name).stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    // Fallback methods
    public UserResponse createUserFallback(CreateUserRequest request, Exception ex) {
        logger.error("Fallback method called for createUser: {}", ex.getMessage());
        throw new RuntimeException("User service is temporarily unavailable. Please try again later.");
    }
    
    public UserResponse updateUserFallback(Long id, UpdateUserRequest request, Exception ex) {
        logger.error("Fallback method called for updateUser: {}", ex.getMessage());
        throw new RuntimeException("User service is temporarily unavailable. Please try again later.");
    }
    
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getStatus().toString(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}