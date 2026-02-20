package com.microservices.user.controller;

import com.microservices.common.dto.BaseResponse;
import com.microservices.user.dto.CreateUserRequest;
import com.microservices.user.dto.UpdateUserRequest;
import com.microservices.user.dto.UserResponse;
import com.microservices.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Creating user with username: {}", request.getUsername());
        
        UserResponse user = userService.createUser(request);
        BaseResponse<UserResponse> response = BaseResponse.success("User created successfully", user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        
        UserResponse user = userService.getUserById(id);
        BaseResponse<UserResponse> response = BaseResponse.success(user);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        logger.info("Fetching user with username: {}", username);
        
        UserResponse user = userService.getUserByUsername(username);
        BaseResponse<UserResponse> response = BaseResponse.success(user);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllUsers() {
        logger.info("Fetching all users");
        
        List<UserResponse> users = userService.getAllUsers();
        BaseResponse<List<UserResponse>> response = BaseResponse.success(users);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUserRequest request) {
        logger.info("Updating user with ID: {}", id);
        
        UserResponse user = userService.updateUser(id, request);
        BaseResponse<UserResponse> response = BaseResponse.success("User updated successfully", user);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        
        userService.deleteUser(id);
        BaseResponse<Void> response = BaseResponse.success("User deleted successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<UserResponse>>> searchUsers(@RequestParam String name) {
        logger.info("Searching users by name: {}", name);
        
        List<UserResponse> users = userService.searchUsersByName(name);
        BaseResponse<List<UserResponse>> response = BaseResponse.success(users);
        
        return ResponseEntity.ok(response);
    }
}