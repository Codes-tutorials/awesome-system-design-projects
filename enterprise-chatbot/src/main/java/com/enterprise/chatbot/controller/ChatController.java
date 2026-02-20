package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.dto.ChatRequest;
import com.enterprise.chatbot.dto.ChatResponse;
import com.enterprise.chatbot.model.ChatMessage;
import com.enterprise.chatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "APIs for chat functionality")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/session")
    @Operation(summary = "Create a new chat session")
    public ResponseEntity<String> createSession(@RequestParam(required = false) String userId) {
        String sessionId = chatService.createChatSession(userId != null ? userId : "anonymous");
        return ResponseEntity.ok(sessionId);
    }
    
    @PostMapping("/message")
    @Operation(summary = "Send a message to the chatbot")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            String response = chatService.processMessage(request.getSessionId(), request.getMessage());
            return ResponseEntity.ok(new ChatResponse(response, System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ChatResponse("Error processing message: " + e.getMessage(), System.currentTimeMillis()));
        }
    }
    
    @GetMapping("/history/{sessionId}")
    @Operation(summary = "Get chat history for a session")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatService.getChatHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/sessions/{userId}")
    @Operation(summary = "Get all sessions for a user")
    public ResponseEntity<List<com.enterprise.chatbot.model.ChatSession>> getUserSessions(@PathVariable String userId) {
        List<com.enterprise.chatbot.model.ChatSession> sessions = chatService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }
    
    // WebSocket endpoint for real-time chat
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatResponse sendMessageWebSocket(ChatRequest chatRequest) {
        String response = chatService.processMessage(chatRequest.getSessionId(), chatRequest.getMessage());
        return new ChatResponse(response, System.currentTimeMillis());
    }
}