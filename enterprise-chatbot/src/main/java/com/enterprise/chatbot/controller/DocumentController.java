package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.model.Document;
import com.enterprise.chatbot.service.DocumentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Management", description = "APIs for document upload and processing")
public class DocumentController {
    
    @Autowired
    private DocumentProcessingService documentProcessingService;
    
    @PostMapping("/upload")
    @Operation(summary = "Upload a document for processing")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && 
                !contentType.startsWith("text/") && 
                !contentType.equals("application/msword") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                return ResponseEntity.badRequest().body("Unsupported file type. Please upload PDF, text, or Word documents.");
            }
            
            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size exceeds 10MB limit");
            }
            
            Document document = documentProcessingService.uploadDocument(file);
            return ResponseEntity.ok(document);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error uploading document: " + e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentProcessingService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status")
    public ResponseEntity<List<Document>> getDocumentsByStatus(@PathVariable String status) {
        List<Document> documents = documentProcessingService.getDocumentsByStatus(status);
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        // This would need to be implemented in the service
        return ResponseEntity.notFound().build();
    }
}