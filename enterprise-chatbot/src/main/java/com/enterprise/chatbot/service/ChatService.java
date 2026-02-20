package com.enterprise.chatbot.service;

import com.enterprise.chatbot.model.ChatMessage;
import com.enterprise.chatbot.model.ChatSession;
import com.enterprise.chatbot.repository.ChatMessageRepository;
import com.enterprise.chatbot.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired
    private ChatClient chatClient;
    
    @Autowired
    private VectorStore vectorStore;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public String createChatSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId, userId);
        chatSessionRepository.save(session);
        
        // Cache session in Redis for 24 hours
        redisTemplate.opsForValue().set("session:" + sessionId, session, 24, TimeUnit.HOURS);
        
        logger.info("Created new chat session: {} for user: {}", sessionId, userId);
        return sessionId;
    }
    
    public String processMessage(String sessionId, String userMessage) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Get or create session
            ChatSession session = getOrCreateSession(sessionId);
            
            // Save user message
            ChatMessage userMsg = new ChatMessage(session, "USER", userMessage);
            chatMessageRepository.save(userMsg);
            
            // Get relevant context from vector store
            String context = getRelevantContext(userMessage);
            
            // Generate response using ChatClient
            String response = generateResponse(userMessage, context, session);
            
            // Save assistant response
            ChatMessage assistantMsg = new ChatMessage(session, "ASSISTANT", response);
            long responseTime = System.currentTimeMillis() - startTime;
            assistantMsg.setResponseTimeMs(responseTime);
            chatMessageRepository.save(assistantMsg);
            
            // Update session activity
            session.setLastActivity(LocalDateTime.now());
            chatSessionRepository.save(session);
            
            logger.info("Processed message for session: {} in {}ms", sessionId, responseTime);
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing message for session: {}", sessionId, e);
            return "I apologize, but I encountered an error processing your request. Please try again.";
        }
    }
    
    private ChatSession getOrCreateSession(String sessionId) {
        // Try to get from cache first
        ChatSession session = (ChatSession) redisTemplate.opsForValue().get("session:" + sessionId);
        
        if (session == null) {
            // Try to get from database
            session = chatSessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    // Create new session if not found
                    ChatSession newSession = new ChatSession(sessionId, "anonymous");
                    return chatSessionRepository.save(newSession);
                });
            
            // Cache the session
            redisTemplate.opsForValue().set("session:" + sessionId, session, 24, TimeUnit.HOURS);
        }
        
        return session;
    }
    
    private String getRelevantContext(String query) {
        try {
            // Search for relevant documents
            SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(5)
                .withSimilarityThreshold(0.7);
            
            List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
            
            if (relevantDocs.isEmpty()) {
                return "No relevant context found in the knowledge base.";
            }
            
            // Combine relevant document content
            String context = relevantDocs.stream()
                .map(doc -> {
                    String source = doc.getMetadata().getOrDefault("source", "Unknown").toString();
                    return String.format("Source: %s\nContent: %s", source, doc.getContent());
                })
                .collect(Collectors.joining("\n\n---\n\n"));
            
            logger.debug("Found {} relevant documents for query", relevantDocs.size());
            return context;
            
        } catch (Exception e) {
            logger.error("Error retrieving context for query: {}", query, e);
            return "Error retrieving context from knowledge base.";
        }
    }
    
    private String generateResponse(String userMessage, String context, ChatSession session) {
        // Get recent conversation history
        List<ChatMessage> recentMessages = chatMessageRepository
            .findByChatSessionIdOrderByCreatedAtAsc(session.getId())
            .stream()
            .skip(Math.max(0, chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(session.getId()).size() - 10))
            .collect(Collectors.toList());
        
        // Build conversation history
        StringBuilder conversationHistory = new StringBuilder();
        for (ChatMessage msg : recentMessages) {
            conversationHistory.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
        }
        
        // Create the prompt
        String prompt = String.format("""
            You are an enterprise assistant. Use the following context to answer the user's question.
            If the context doesn't contain relevant information, say so clearly and provide general guidance if appropriate.
            
            Context from knowledge base:
            %s
            
            Recent conversation history:
            %s
            
            User question: %s
            
            Please provide a helpful, accurate response based on the available context.
            """, context, conversationHistory.toString(), userMessage);
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
    
    public List<ChatMessage> getChatHistory(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId).orElse(null);
        if (session == null) {
            return List.of();
        }
        
        return chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(session.getId());
    }
    
    public List<ChatSession> getUserSessions(String userId) {
        return chatSessionRepository.findByUserId(userId);
    }
}