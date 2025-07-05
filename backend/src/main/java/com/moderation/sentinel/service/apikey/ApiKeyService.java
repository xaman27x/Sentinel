package com.moderation.sentinel.service.apikey;

import com.moderation.sentinel.model.ApiKey;
import com.moderation.sentinel.repository.ApiKeyRepository;
import com.moderation.sentinel.security.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private EncryptionService encryptionService;
    
    private static final int MAX_KEYS_PER_USER = 10;
    
    public ApiKey createApiKey(Long userId, String keyName) {
        
        long activeKeysCount = apiKeyRepository.countActiveKeysByUserId(userId);
        if (activeKeysCount >= MAX_KEYS_PER_USER) {
            throw new RuntimeException("Maximum number of API keys reached for user");
        }
        
        String apiKey = encryptionService.generateApiKey();
        
        while (apiKeyRepository.existsByApiKey(apiKey)) {
            apiKey = encryptionService.generateApiKey();
        }
        
        ApiKey newApiKey = new ApiKey(userId, apiKey, keyName);
        return apiKeyRepository.save(newApiKey);
    }
    
    public List<ApiKey> getUserApiKeys(Long userId) {
        return apiKeyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<ApiKey> getActiveUserApiKeys(Long userId) {
        return apiKeyRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    public boolean revokeApiKey(UUID apiKeyId, Long userId) {
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findById(apiKeyId);
        
        if (apiKeyOpt.isPresent()) {
            ApiKey apiKey = apiKeyOpt.get();
            
            // Verify ownership
            if (!apiKey.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized access to API key");
            }
            
            apiKey.revoke();
            apiKeyRepository.save(apiKey);
            return true;
        }
        
        return false;
    }
    
    public boolean revokeApiKeyByString(String apiKey, Long userId) {
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey);
        
        if (apiKeyOpt.isPresent()) {
            ApiKey key = apiKeyOpt.get();
            
            // Verify ownership
            if (!key.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized access to API key");
            }
            
            key.revoke();
            apiKeyRepository.save(key);
            return true;
        }
        
        return false;
    }
    
    public Optional<ApiKey> getApiKeyById(UUID apiKeyId) {
        return apiKeyRepository.findById(apiKeyId);
    }
    
    public void cleanupInactiveKeys(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<ApiKey> inactiveKeys = apiKeyRepository.findInactiveKeys(cutoffDate);
        
        for (ApiKey key : inactiveKeys) {
            key.revoke();
        }
        
        apiKeyRepository.saveAll(inactiveKeys);
    }
    
    public boolean validateApiKey(String apiKey) {
        return apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey).isPresent();
    }
    
    public ApiKey getByApiKey(String apiKey) {
        return apiKeyRepository.findByApiKeyAndIsActiveTrue(apiKey).orElse(null);
    }
}