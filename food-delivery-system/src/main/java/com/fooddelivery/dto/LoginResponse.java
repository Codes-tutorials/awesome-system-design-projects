package com.fooddelivery.dto;

import com.fooddelivery.model.User;

/**
 * DTO for login response containing user info and JWT token
 */
public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private String refreshToken;
    private Long expiresIn;
    private User user;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String refreshToken, Long expiresIn, User user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}