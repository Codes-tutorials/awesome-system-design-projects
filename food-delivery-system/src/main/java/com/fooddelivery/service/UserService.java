package com.fooddelivery.service;

import com.fooddelivery.model.User;
import com.fooddelivery.model.UserRole;
import com.fooddelivery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for User entity operations
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private NotificationService notificationService;
    
    public User createUser(String firstName, String lastName, String email, String phoneNumber, 
                          String password, UserRole role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }
        
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("User with phone number " + phoneNumber + " already exists");
        }
        
        // Create new user
        User user = new User(firstName, lastName, email, phoneNumber, 
                           passwordEncoder.encode(password), role);
        user.setVerificationToken(generateVerificationToken());
        
        User savedUser = userRepository.save(user);
        
        // Send verification email
        notificationService.sendVerificationEmail(savedUser);
        
        return savedUser;
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    public User updateUser(Long userId, String firstName, String lastName, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        // Check if phone number is being changed and if it's already taken
        if (!user.getPhoneNumber().equals(phoneNumber)) {
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new RuntimeException("Phone number " + phoneNumber + " is already taken");
            }
            user.setPhoneNumber(phoneNumber);
        }
        
        return userRepository.save(user);
    }
    
    public User updateProfilePicture(Long userId, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }
    
    public boolean verifyUser(String verificationToken) {
        Optional<User> userOpt = userRepository.findByVerificationToken(verificationToken);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return true;
        }
        
        return false;
    }
    
    public boolean initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = generateResetToken();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordExpiresAt(LocalDateTime.now().plusHours(1)); // 1 hour expiry
            
            userRepository.save(user);
            notificationService.sendPasswordResetEmail(user, resetToken);
            return true;
        }
        
        return false;
    }
    
    public boolean resetPassword(String resetToken, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetPasswordToken(resetToken);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Check if token is not expired
            if (user.getResetPasswordExpiresAt().isAfter(LocalDateTime.now())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                user.setResetPasswordToken(null);
                user.setResetPasswordExpiresAt(null);
                userRepository.save(user);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Verify current password
        if (passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        
        return false;
    }
    
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setIsActive(false);
        return userRepository.save(user);
    }
    
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setIsActive(true);
        return userRepository.save(user);
    }
    
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findActiveUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }
    
    public List<User> findRecentlyActiveUsers(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return userRepository.findRecentlyActiveUsers(since);
    }
    
    public Long countActiveUsersByRole(UserRole role) {
        return userRepository.countActiveUsersByRole(role);
    }
    
    public void cleanupExpiredResetTokens() {
        List<User> usersWithExpiredTokens = userRepository.findUsersWithExpiredResetTokens(LocalDateTime.now());
        
        for (User user : usersWithExpiredTokens) {
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiresAt(null);
        }
        
        userRepository.saveAll(usersWithExpiredTokens);
    }
    
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}