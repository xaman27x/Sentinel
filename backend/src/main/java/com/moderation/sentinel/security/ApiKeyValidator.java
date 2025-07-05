package com.moderation.sentinel.security;

import com.moderation.sentinel.model.ApiKey;
import com.moderation.sentinel.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyValidator {
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        Optional<ApiKey> keyOptional = apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey);
        
        if (keyOptional.isPresent()) {
            ApiKey key = keyOptional.get();
            key.setLastUsed(LocalDateTime.now());
            key.incrementUsage();
            apiKeyRepository.save(key);
            return true;
        }
        
        return false;
    }
    
    public ApiKey getApiKeyDetails(String apiKey) {
        return apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey).orElse(null);
    }
    
    public ApiKey getApiKeyDetailsById(UUID apiKeyId) {
        return apiKeyRepository.findById(apiKeyId).orElse(null);
    }
    
    public boolean belongsToUser(String apiKey, Long userId) {
        Optional<ApiKey> keyOptional = apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey);
        return keyOptional.isPresent() && keyOptional.get().getUserId().equals(userId);
    }
    
    public boolean belongsToUserById(UUID apiKeyId, Long userId) {
        Optional<ApiKey> keyOptional = apiKeyRepository.findById(apiKeyId);
        return keyOptional.isPresent() && keyOptional.get().getUserId().equals(userId);
    }
}