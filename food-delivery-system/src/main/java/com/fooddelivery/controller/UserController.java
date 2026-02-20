package com.fooddelivery.controller;

import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.model.User;
import com.fooddelivery.model.UserRole;
import com.fooddelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for User management operations
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for user registration, profile management, and authentication")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with specified role")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = userService.createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getPassword(),
                request.getRole()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve user details by email address")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                         @RequestParam String firstName,
                                         @RequestParam String lastName,
                                         @RequestParam String phoneNumber) {
        try {
            User updatedUser = userService.updateUser(id, firstName, lastName, phoneNumber);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/profile-picture")
    @Operation(summary = "Update profile picture", description = "Update user profile picture URL")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<User> updateProfilePicture(@PathVariable Long id,
                                                    @RequestParam String profilePictureUrl) {
        try {
            User updatedUser = userService.updateProfilePicture(id, profilePictureUrl);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify user account", description = "Verify user account using verification token")
    public ResponseEntity<Map<String, String>> verifyUser(@RequestParam String token) {
        boolean verified = userService.verifyUser(token);
        if (verified) {
            return ResponseEntity.ok(Map.of("message", "Account verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired verification token"));
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset", description = "Send password reset email to user")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        boolean initiated = userService.initiatePasswordReset(email);
        if (initiated) {
            return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Email not found"));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestParam String token,
                                                            @RequestParam String newPassword) {
        boolean reset = userService.resetPassword(token, newPassword);
        if (reset) {
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired reset token"));
        }
    }
    
    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Change user password with current password verification")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable Long id,
                                                             @RequestParam String currentPassword,
                                                             @RequestParam String newPassword) {
        boolean changed = userService.changePassword(id, currentPassword, newPassword);
        if (changed) {
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }
    }
    
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        try {
            User deactivatedUser = userService.deactivateUser(id);
            return ResponseEntity.ok(deactivatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activate user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        try {
            User activatedUser = userService.activateUser(id);
            return ResponseEntity.ok(activatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve all users with specified role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.findUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/active/role/{role}")
    @Operation(summary = "Get active users by role", description = "Retrieve all active users with specified role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getActiveUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.findActiveUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/recent-active")
    @Operation(summary = "Get recently active users", description = "Retrieve users who were active in the last specified hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getRecentlyActiveUsers(@RequestParam(defaultValue = "24") int hours) {
        List<User> users = userService.findRecentlyActiveUsers(hours);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/count/role/{role}")
    @Operation(summary = "Count users by role", description = "Get count of active users with specified role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countUsersByRole(@PathVariable UserRole role) {
        Long count = userService.countActiveUsersByRole(role);
        return ResponseEntity.ok(Map.of("count", count, "role", role.name()));
    }
    
    @PostMapping("/cleanup-expired-tokens")
    @Operation(summary = "Cleanup expired tokens", description = "Remove expired password reset tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupExpiredTokens() {
        userService.cleanupExpiredResetTokens();
        return ResponseEntity.ok(Map.of("message", "Expired tokens cleaned up successfully"));
    }
}