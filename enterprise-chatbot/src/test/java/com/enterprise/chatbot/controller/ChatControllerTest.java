package com.enterprise.chatbot.controller;

import com.enterprise.chatbot.dto.ChatRequest;
import com.enterprise.chatbot.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateSession() throws Exception {
        when(chatService.createChatSession(anyString())).thenReturn("test-session-id");

        mockMvc.perform(post("/api/chat/session")
                .param("userId", "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().string("test-session-id"));
    }

    @Test
    void testSendMessage() throws Exception {
        ChatRequest request = new ChatRequest("test-session-id", "Hello, how are you?");
        when(chatService.processMessage(anyString(), anyString()))
                .thenReturn("Hello! I'm doing well, thank you for asking.");

        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello! I'm doing well, thank you for asking."))
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void testSendMessageWithInvalidRequest() throws Exception {
        ChatRequest request = new ChatRequest("", ""); // Invalid request

        mockMvc.perform(post("/api/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetChatHistory() throws Exception {
        when(chatService.getChatHistory(anyString())).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/chat/history/test-session-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}