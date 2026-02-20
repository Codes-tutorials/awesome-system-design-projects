package com.fooddelivery.controller;

import com.fooddelivery.dto.LoginRequest;
import com.fooddelivery.dto.LoginResponse;
import com.fooddelivery.dto.UserRegistrationRequest;
import com.fooddelivery.model.User;
import com.fooddelivery.service.AuthService;
import com.fooddelivery.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication, registration, and JWT token management")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = userService.createUser(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getPhoneNumber(),
                    request.getPassword(),
                    request.getRole()
            );
            
            // Generate JWT token for the new user
            String token = authService.generateToken(user);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "User registered successfully",
                    "user", user,
                    "token", token,
                    "tokenType", "Bearer"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/login/phone")
    @Operation(summary = "Login with phone", description = "Authenticate user using phone number and password")
    public ResponseEntity<LoginResponse> loginWithPhone(@RequestParam String phoneNumber,
                                                       @RequestParam String password) {
        try {
            LoginResponse response = authService.authenticateWithPhone(phoneNumber, password);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token using refresh token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam String refreshToken) {
        try {
            Map<String, Object> response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            authService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", description = "Logout user from all devices and invalidate all tokens")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logoutFromAllDevices(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            authService.logoutFromAllDevices(userId);
            return ResponseEntity.ok(Map.of("message", "Logged out from all devices successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email using verification token")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        try {
            boolean verified = userService.verifyUser(token);
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired verification token"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email", description = "Resend email verification link")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of("message", "Verification email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiate password reset process")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        try {
            boolean initiated = userService.initiatePasswordReset(email);
            if (initiated) {
                return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Email not found"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestParam String token,
                                                            @RequestParam String newPassword) {
        try {
            boolean reset = userService.resetPassword(token, newPassword);
            if (reset) {
                return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired reset token"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@RequestParam String currentPassword,
                                                             @RequestParam String newPassword,
                                                             Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            boolean changed = userService.changePassword(userId, currentPassword, newPassword);
            if (changed) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            return userService.findById(userId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/validate-token")
    @Operation(summary = "Validate token", description = "Validate JWT token and return user information")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestParam String token) {
        try {
            Map<String, Object> validation = authService.validateToken(token);
            return ResponseEntity.ok(validation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/social/google")
    @Operation(summary = "Google OAuth login", description = "Authenticate using Google OAuth token")
    public ResponseEntity<LoginResponse> googleLogin(@RequestParam String googleToken) {
        try {
            LoginResponse response = authService.authenticateWithGoogle(googleToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/social/facebook")
    @Operation(summary = "Facebook OAuth login", description = "Authenticate using Facebook OAuth token")
    public ResponseEntity<LoginResponse> facebookLogin(@RequestParam String facebookToken) {
        try {
            LoginResponse response = authService.authenticateWithFacebook(facebookToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP", description = "Send OTP for phone verification or login")
    public ResponseEntity<Map<String, String>> sendOTP(@RequestParam String phoneNumber,
                                                      @RequestParam(defaultValue = "LOGIN") String purpose) {
        try {
            authService.sendOTP(phoneNumber, purpose);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP", description = "Verify OTP and authenticate user")
    public ResponseEntity<LoginResponse> verifyOTP(@RequestParam String phoneNumber,
                                                  @RequestParam String otp) {
        try {
            LoginResponse response = authService.verifyOTPAndAuthenticate(phoneNumber, otp);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/device/register")
    @Operation(summary = "Register device", description = "Register device for push notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> registerDevice(@RequestParam String deviceToken,
                                                             @RequestParam String deviceType,
                                                             Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            authService.registerDevice(userId, deviceToken, deviceType);
            return ResponseEntity.ok(Map.of("message", "Device registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/device/unregister")
    @Operation(summary = "Unregister device", description = "Unregister device from push notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> unregisterDevice(@RequestParam String deviceToken,
                                                               Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            authService.unregisterDevice(userId, deviceToken);
            return ResponseEntity.ok(Map.of("message", "Device unregistered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/sessions")
    @Operation(summary = "Get active sessions", description = "Get all active sessions for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getActiveSessions(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            Map<String, Object> sessions = authService.getActiveSessions(userId);
            return ResponseEntity.ok(sessions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/sessions/{sessionId}/revoke")
    @Operation(summary = "Revoke session", description = "Revoke specific session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> revokeSession(@PathVariable String sessionId,
                                                           Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            authService.revokeSession(userId, sessionId);
            return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper methods
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Extract user ID from authentication principal
        // Implementation depends on your authentication setup
        return 1L; // Placeholder
    }
}