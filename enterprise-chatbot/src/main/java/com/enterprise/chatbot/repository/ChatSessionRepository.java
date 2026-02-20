package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    Optional<ChatSession> findBySessionId(String sessionId);
    
    List<ChatSession> findByUserId(String userId);
    
    List<ChatSession> findByLastActivityAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.createdAt >= :startDate")
    long countSessionsCreatedAfter(LocalDateTime startDate);
}