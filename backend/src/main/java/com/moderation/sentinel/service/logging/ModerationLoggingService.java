package com.moderation.sentinel.service.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moderation.sentinel.model.ModerationLogs;
import com.moderation.sentinel.model.ModerationResponse;
import com.moderation.sentinel.repository.ModerationLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ModerationLoggingService {
    
    @Autowired
    private ModerationLogsRepository moderationLogsRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public void logModerationRequest(Long userId, UUID apiKeyId, String inputText,
                                   ModerationResponse response, Long processingTimeMs,
                                   String clientIp, String userAgent) {
        try {
            String detectedTermsJson = objectMapper.writeValueAsString(response.offensiveTerms);
            
            ModerationLogs log = new ModerationLogs();
            log.setUserId(userId);
            log.setApiKeyId(apiKeyId);
            log.setInputText(inputText);
            log.setIsOffensive(response.isOffensive);
            log.setConfidenceScore(response.confidence);
            log.setDetectedTerms(detectedTermsJson);
            log.setProcessingTimeMs(processingTimeMs);
            log.setClientIp(clientIp);
            log.setUserAgent(userAgent);
            
            moderationLogsRepository.save(log);
            
        } catch (JsonProcessingException e) {
            System.err.println("Failed to serialize detected terms: " + e.getMessage());
        }
    }
    
    public List<ModerationLogs> getUserLogs(Long userId, int limit) {
        return moderationLogsRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<ModerationLogs> getApiKeyLogs(String apiKeyId, int limit) {
        return moderationLogsRepository.findByApiKeyIdOrderByCreatedAtDesc(UUID.fromString(apiKeyId));
    }


}