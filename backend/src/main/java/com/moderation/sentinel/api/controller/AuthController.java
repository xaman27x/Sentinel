package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.api.dto.request.LoginRequest;
import com.moderation.sentinel.api.dto.request.RegisterRequest;
import com.moderation.sentinel.api.dto.response.ApiResponse;
import com.moderation.sentinel.api.dto.response.AuthResponse;
import com.moderation.sentinel.model.User;
import com.moderation.sentinel.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Value("${application.jwt.expiration:86400000}")
    private long jwtExpirationMs;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(
                request.getEmail(),
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
            );
            
            String token = authService.authenticateUser(request.getEmail(), request.getPassword());
            
            AuthResponse authResponse = new AuthResponse(
                token,
                jwtExpirationMs / 1000, // Convert to seconds
                user.getUserId(),
                user.getEmail(),
                user.getRole()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage(), "REGISTRATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.authenticateUser(request.getEmail(), request.getPassword());
            User user = authService.getCurrentUser(request.getEmail());
            
            AuthResponse authResponse = new AuthResponse(
                token,
                jwtExpirationMs / 1000,
                user.getUserId(),
                user.getEmail(),
                user.getRole()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage(), "LOGIN_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = authService.validateToken(token);
                return ResponseEntity.ok(ApiResponse.success("Token validation result", isValid));
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid authorization header", "INVALID_AUTH_HEADER"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
}