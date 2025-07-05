package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.api.dto.request.BatchModerationRequest;
import com.moderation.sentinel.api.dto.request.TextModerationRequest;
import com.moderation.sentinel.api.dto.response.ApiResponse;
import com.moderation.sentinel.model.ApiKey;
import com.moderation.sentinel.model.ModerationResponse;
import com.moderation.sentinel.service.apikey.ApiKeyService;
import com.moderation.sentinel.service.logging.ModerationLoggingService;
import com.moderation.sentinel.service.moderation.ModerationService;
import com.moderation.sentinel.service.ratelimit.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderate")
@CrossOrigin(origins = "*")
public class ModerationController {
    
    @Autowired
    private ModerationService moderationService;
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private ModerationLoggingService loggingService;
    
    @PostMapping("/text")
    public ResponseEntity<ApiResponse<ModerationResponse>> moderateText(
            @Valid @RequestBody TextModerationRequest request,
            @RequestHeader("X-API-Key") String apiKey,
            HttpServletRequest httpRequest) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // we validate the key
            if (!apiKeyService.validateApiKey(apiKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid API key", "INVALID_API_KEY"));
            }
            
            ApiKey apiKeyDetails = apiKeyService.getByApiKey(apiKey);
            if (apiKeyDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("API key not found", "API_KEY_NOT_FOUND"));
            }

            // Check rate limit
            boolean rateLimitExceeded = rateLimitService.isRateLimitExceeded(
                    apiKeyDetails.getUserId(),
                    apiKeyDetails.getApiKeyId()
            );

            if (rateLimitExceeded) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error("Rate limit exceeded", "RATE_LIMIT_EXCEEDED"));
            }

            // Lets update the rate limit
            rateLimitService.recordRequest(apiKeyDetails.getUserId(), apiKeyDetails.getApiKeyId());


            // Perform moderation
            ModerationResponse result = moderationService.analyze(request.getText());
            
            // Apply custom confidence threshold if provided
            if (request.getConfidenceThreshold() != null) {
                boolean isOffensive = result.confidence >= request.getConfidenceThreshold() && 
                                    !result.offensiveTerms.isEmpty();
                result = new ModerationResponse(
                    isOffensive,
                    result.confidence,
                    result.message,
                    request.isReturnDetails() ? result.offensiveTerms : Map.of()
                );
            }

            // Log the request in db
            long processingTime = System.currentTimeMillis() - startTime;
            loggingService.logModerationRequest(
                apiKeyDetails.getUserId(),
                apiKeyDetails.getApiKeyId(),
                request.getText(),
                result,
                processingTime,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
            );

            
            return ResponseEntity.ok(ApiResponse.success("Text moderated successfully", result));
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(e + " :Internal server error", "INTERNAL_ERROR"));
        }
    }

    /*
       * /batch will process high volume data stream, controlled via Kafka.
       * Kafka Implementation is pending
     */
    
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ModerationResponse>>> moderateBatch(
            @Valid @RequestBody BatchModerationRequest request,
            @RequestHeader("X-API-Key") String apiKey,
            HttpServletRequest httpRequest) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate API key
            if (!apiKeyService.validateApiKey(apiKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid API key", "INVALID_API_KEY"));
            }
            
            ApiKey apiKeyDetails = apiKeyService.getByApiKey(apiKey);

            boolean rateLimitExceeded = rateLimitService.isRateLimitExceeded(
                apiKeyDetails.getUserId(),
                    UUID.fromString(apiKey),
                request.getTexts().size()
            );
            
            if (rateLimitExceeded) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Rate limit exceeded", "RATE_LIMIT_EXCEEDED"));
            }
            
            // Process batch
            List<ModerationResponse> results = new ArrayList<>();
            
            for (String text : request.getTexts()) {
                ModerationResponse result = moderationService.analyze(text);

                if (request.getConfidenceThreshold() != null) {
                    boolean isOffensive = result.confidence >= request.getConfidenceThreshold() && 
                                        !result.offensiveTerms.isEmpty();
                    result = new ModerationResponse(
                        isOffensive,
                        result.confidence,
                        result.message,
                        request.isReturnDetails() ? result.offensiveTerms : Map.of()
                    );
                }
                
                results.add(result);

                loggingService.logModerationRequest(
                    apiKeyDetails.getUserId(),
                    apiKeyDetails.getApiKeyId(),
                    text,
                    result,
                    0L,
                    getClientIp(httpRequest),
                    httpRequest.getHeader("User-Agent")
                );
            }
            
            // Rate limit updated
            rateLimitService.recordRequests(apiKeyDetails.getUserId(), apiKeyDetails.getApiKeyId(), request.getTexts().size());
            
            return ResponseEntity.ok(ApiResponse.success("Batch moderated successfully", results));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = Map.of(
            "status", "healthy",
            "service", "content-moderation",
            "version", "1.0.0",
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Service is healthy", health));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}