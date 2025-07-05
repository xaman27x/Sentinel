package com.moderation.sentinel.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private long expiresIn;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("role")
    private String role;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, long expiresIn, Long userId, String email, String role) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}