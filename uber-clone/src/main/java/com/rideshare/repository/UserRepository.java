package com.rideshare.repository;

import com.rideshare.model.User;
import com.rideshare.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Find user by email or phone number
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phoneNumber = :identifier")
    Optional<User> findByEmailOrPhoneNumber(@Param("identifier") String identifier);
    
    /**
     * Find active users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    java.util.List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    /**
     * Count users by role
     */
    long countByRole(UserRole role);
    
    /**
     * Find users created in the last N days
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= CURRENT_TIMESTAMP - :days DAY")
    java.util.List<User> findUsersCreatedInLastDays(@Param("days") int days);
}