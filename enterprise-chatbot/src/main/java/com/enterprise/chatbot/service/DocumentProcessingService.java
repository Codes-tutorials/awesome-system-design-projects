package com.enterprise.chatbot.service;

import com.enterprise.chatbot.model.Document;
import com.enterprise.chatbot.repository.DocumentRepository;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private VectorStore vectorStore;
    
    private final Tika tika = new Tika();
    
    public Document uploadDocument(MultipartFile file) throws IOException {
        Document document = new Document(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize()
        );
        
        document = documentRepository.save(document);
        
        // Process document asynchronously
        processDocumentAsync(document.getId(), file.getBytes());
        
        return document;
    }
    
    @Async
    public CompletableFuture<Void> processDocumentAsync(Long documentId, byte[] fileContent) {
        try {
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
            
            String extractedText = extractText(fileContent, document.getContentType());
            
            // Store extracted text
            document.setExtractedText(extractedText);
            document.setStatus("COMPLETED");
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);
            
            // Create embeddings and store in vector database
            createAndStoreEmbeddings(document, extractedText);
            
            logger.info("Successfully processed document: {}", document.getFilename());
            
        } catch (Exception e) {
            logger.error("Error processing document ID: {}", documentId, e);
            updateDocumentStatus(documentId, "FAILED");
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private String extractText(byte[] fileContent, String contentType) throws IOException {
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        if (contentType != null && contentType.equals("application/pdf")) {
            DocumentReader pdfReader = new PagePdfDocumentReader(resource);
            List<org.springframework.ai.document.Document> documents = pdfReader.get();
            return documents.stream()
                .map(org.springframework.ai.document.Document::getContent)
                .reduce("", (a, b) -> a + "\n" + b);
        } else {
            DocumentReader tikaReader = new TikaDocumentReader(resource);
            List<org.springframework.ai.document.Document> documents = tikaReader.get();
            return documents.stream()
                .map(org.springframework.ai.document.Document::getContent)
                .reduce("", (a, b) -> a + "\n" + b);
        }
    }
    
    private void createAndStoreEmbeddings(Document document, String text) {
        try {
            // Split text into chunks for better embedding
            List<String> chunks = splitTextIntoChunks(text, 1000);
            
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                org.springframework.ai.document.Document aiDocument = 
                    new org.springframework.ai.document.Document(chunk);
                
                // Add metadata
                aiDocument.getMetadata().put("source", document.getFilename());
                aiDocument.getMetadata().put("document_id", document.getId().toString());
                aiDocument.getMetadata().put("chunk_index", String.valueOf(i));
                aiDocument.getMetadata().put("content_type", document.getContentType());
                
                vectorStore.add(List.of(aiDocument));
            }
            
            logger.info("Created embeddings for document: {} ({} chunks)", 
                document.getFilename(), chunks.size());
                
        } catch (Exception e) {
            logger.error("Error creating embeddings for document: {}", document.getFilename(), e);
        }
    }
    
    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new java.util.ArrayList<>();
        int start = 0;
        
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            
            // Try to break at word boundary
            if (end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }
            
            chunks.add(text.substring(start, end).trim());
            start = end;
        }
        
        return chunks;
    }
    
    private void updateDocumentStatus(Long documentId, String status) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setStatus(status);
                documentRepository.save(document);
            }
        } catch (Exception e) {
            logger.error("Error updating document status", e);
        }
    }
    
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    public List<Document> getDocumentsByStatus(String status) {
        return documentRepository.findByStatus(status);
    }
}