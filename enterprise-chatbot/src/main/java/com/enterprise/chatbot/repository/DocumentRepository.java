package com.enterprise.chatbot.repository;

import com.enterprise.chatbot.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByStatus(String status);
    
    List<Document> findByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = 'COMPLETED'")
    long countProcessedDocuments();
    
    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.status = 'COMPLETED'")
    Long getTotalProcessedFileSize();
}