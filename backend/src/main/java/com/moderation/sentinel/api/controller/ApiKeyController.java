package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.api.dto.response.ApiResponse;
import com.moderation.sentinel.model.ApiKey;
import com.moderation.sentinel.security.JwtTokenProvider;
import com.moderation.sentinel.service.apikey.ApiKeyService;
import com.moderation.sentinel.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
@CrossOrigin(origins = "*")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ApiKey>> createApiKey(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = extractToken(authHeader);

            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String keyName = request.getOrDefault("name", "Default Key");

            ApiKey apiKey = apiKeyService.createApiKey(userId, keyName);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("API key created successfully", apiKey));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid authorization header format", "INVALID_AUTH_HEADER"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "TOKEN_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ApiKey>>> listApiKeys(
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = extractToken(authHeader);

            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            List<ApiKey> apiKeys = apiKeyService.getUserApiKeys(userId);

            return ResponseEntity.ok(ApiResponse.success("API keys retrieved successfully", apiKeys));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid authorization header format", "INVALID_AUTH_HEADER"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "TOKEN_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<ApiResponse<String>> revokeApiKey(
            @PathVariable UUID keyId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = extractToken(authHeader);

            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            boolean revoked = apiKeyService.revokeApiKey(keyId, userId);

            if (revoked) {
                return ResponseEntity.ok(ApiResponse.success("API key revoked successfully", "Key revoked"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("API key not found or doesn't belong to user", "KEY_NOT_FOUND"));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid authorization header format", "INVALID_AUTH_HEADER"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), "TOKEN_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }

        String token = authHeader.substring(7);
        if (token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be empty");
        }

        return token;
    }
}