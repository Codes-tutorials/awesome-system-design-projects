package com.enterprise.chatbot.dto;

public class ChatResponse {
    
    private String message;
    private long timestamp;
    private String status;
    
    // Constructors
    public ChatResponse() {}
    
    public ChatResponse(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
        this.status = "success";
    }
    
    public ChatResponse(String message, long timestamp, String status) {
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}