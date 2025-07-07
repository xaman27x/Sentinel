package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.api.dto.response.ApiResponse;
import com.moderation.sentinel.api.dto.response.LogStatisticsResponse;
import com.moderation.sentinel.api.dto.response.PageResponse;
import com.moderation.sentinel.model.ModerationLogs;
import com.moderation.sentinel.security.JwtTokenProvider;
import com.moderation.sentinel.service.auth.AuthService;
import com.moderation.sentinel.service.logging.ModerationLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class ModerationLogsController {

    @Autowired
    private ModerationLogsService logsService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PageResponse<ModerationLogs>>> getUserLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        try {
            String token = extractToken(authHeader);
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }
            
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            
            Page<ModerationLogs> logs = logsService.getUserLogs(
                userId, 
                PageRequest.of(page, size, Sort.by(direction, sortField))
            );
            
            PageResponse<ModerationLogs> response = new PageResponse<>(
                logs.getContent(),
                page,
                size,
                logs.getTotalElements(),
                logs.getTotalPages()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Logs retrieved successfully", response));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving logs: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping("/key/{apiKeyId}")
    public ResponseEntity<ApiResponse<PageResponse<ModerationLogs>>> getApiKeyLogs(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("apiKeyId") String apiKeyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        try {
            String token = extractToken(authHeader);
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }
            
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            
            // Validate that the API key belongs to the user
            if (!logsService.validateApiKeyOwnership(userId, UUID.fromString(apiKeyId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to access these logs", "ACCESS_DENIED"));
            }
            
            Page<ModerationLogs> logs = logsService.getApiKeyLogs(
                UUID.fromString(apiKeyId), 
                PageRequest.of(page, size, Sort.by(direction, sortField))
            );
            
            PageResponse<ModerationLogs> response = new PageResponse<>(
                logs.getContent(),
                page,
                size,
                logs.getTotalElements(),
                logs.getTotalPages()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Logs retrieved successfully", response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid API key ID format", "INVALID_PARAMETER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving logs: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<ModerationLogs>> getLogDetails(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("logId") Long logId) {
        
        try {
            String token = extractToken(authHeader);
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }
            
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            ModerationLogs log = logsService.getLogById(logId);
            
            if (log == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Log not found", "RESOURCE_NOT_FOUND"));
            }
            
            // Verify that the user owns this log
            if (!log.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to access this log", "ACCESS_DENIED"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Log details retrieved successfully", log));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving log details: " + e.getMessage(), "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<LogStatisticsResponse>> getStatistics(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "last_30_days") String period) {
        
        try {
            String token = extractToken(authHeader);
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired token", "INVALID_TOKEN"));
            }
            
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            
            LocalDateTime startDate;
            LocalDateTime now = LocalDateTime.now();
            
            switch (period) {
                case "today":
                    startDate = now.toLocalDate().atStartOfDay();
                    break;
                case "last_7_days":
                    startDate = now.minusDays(7);
                    break;
                case "all_time":
                    startDate = LocalDateTime.of(2020, 1, 1, 0, 0); // Far in the past
                    break;
                case "last_30_days":
                default:
                    startDate = now.minusDays(30);
                    break;
            }
            
            LogStatisticsResponse stats = logsService.getStatistics(userId, startDate, now);
            
            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error retrieving statistics: " + e.getMessage(), "INTERNAL_ERROR"));
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