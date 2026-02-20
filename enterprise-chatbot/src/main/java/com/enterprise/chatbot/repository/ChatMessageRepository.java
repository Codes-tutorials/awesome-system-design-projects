package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);
    
    @Query("SELECT AVG(cm.responseTimeMs) FROM ChatMessage cm WHERE cm.role = 'ASSISTANT' AND cm.responseTimeMs IS NOT NULL")
    Double getAverageResponseTime();
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.createdAt >= :startDate")
    long countMessagesAfter(LocalDateTime startDate);
}