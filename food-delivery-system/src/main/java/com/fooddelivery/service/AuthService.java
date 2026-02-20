package com.fooddelivery.service;

import com.fooddelivery.dto.LoginResponse;
import com.fooddelivery.model.User;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service class for Authentication operations
 */
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    public LoginResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new LoginResponse(jwt, refreshToken, tokenProvider.getExpirationTime(), user);
    }
    
    public LoginResponse authenticateWithPhone(String phoneNumber, String password) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return authenticate(user.getEmail(), password);
    }
    
    public String generateToken(User user) {
        return tokenProvider.generateTokenFromEmail(user.getEmail());
    }
    
    public Map<String, Object> refreshToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String email = tokenProvider.getEmailFromToken(refreshToken);
            String newToken = tokenProvider.generateTokenFromEmail(email);
            String newRefreshToken = tokenProvider.generateRefreshToken(email);
            
            return Map.of(
                    "token", newToken,
                    "refreshToken", newRefreshToken,
                    "tokenType", "Bearer",
                    "expiresIn", tokenProvider.getExpirationTime()
            );
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }
    
    public void logout(String token) {
        // Add token to blacklist or invalidate in cache
        // Implementation depends on your token management strategy
    }
    
    public void logoutFromAllDevices(Long userId) {
        // Invalidate all tokens for the user
        // Implementation depends on your token management strategy
    }
    
    public void resendVerificationEmail(String email) {
        // Implementation for resending verification email
    }
    
    public Map<String, Object> validateToken(String token) {
        if (tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmailFromToken(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return Map.of(
                    "valid", true,
                    "email", email,
                    "user", user
            );
        } else {
            throw new RuntimeException("Invalid token");
        }
    }
    
    public LoginResponse authenticateWithGoogle(String googleToken) {
        // Implementation for Google OAuth authentication
        throw new RuntimeException("Google authentication not implemented yet");
    }
    
    public LoginResponse authenticateWithFacebook(String facebookToken) {
        // Implementation for Facebook OAuth authentication
        throw new RuntimeException("Facebook authentication not implemented yet");
    }
    
    public void sendOTP(String phoneNumber, String purpose) {
        // Implementation for sending OTP
        throw new RuntimeException("OTP service not implemented yet");
    }
    
    public LoginResponse verifyOTPAndAuthenticate(String phoneNumber, String otp) {
        // Implementation for OTP verification and authentication
        throw new RuntimeException("OTP verification not implemented yet");
    }
    
    public void registerDevice(Long userId, String deviceToken, String deviceType) {
        // Implementation for device registration
    }
    
    public void unregisterDevice(Long userId, String deviceToken) {
        // Implementation for device unregistration
    }
    
    public Map<String, Object> getActiveSessions(Long userId) {
        // Implementation for getting active sessions
        return Map.of("sessions", "Not implemented yet");
    }
    
    public void revokeSession(Long userId, String sessionId) {
        // Implementation for revoking specific session
    }
}