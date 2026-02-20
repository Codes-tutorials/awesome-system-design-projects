package com.ipl.ticketbooking.repository;

import com.ipl.ticketbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Checks if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Checks if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Finds active users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    java.util.List<User> findActiveUsers();
    
    /**
     * Updates last login time
     */
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
}