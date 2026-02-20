package com.microservices.user.events;

import com.microservices.common.events.BaseEvent;

public class UserUpdatedEvent extends BaseEvent {
    
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    
    public UserUpdatedEvent() {
        super("USER_UPDATED", "user-service");
    }
    
    public UserUpdatedEvent(Long userId, String username, String email, String firstName, String lastName, String status) {
        this();
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}