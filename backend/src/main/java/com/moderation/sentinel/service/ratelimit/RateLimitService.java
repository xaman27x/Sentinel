package com.moderation.sentinel.service.ratelimit;

import com.moderation.sentinel.model.RateLimits;
import com.moderation.sentinel.repository.RateLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RateLimitService {
    
    @Autowired
    private RateLimitRepository rateLimitRepository;
    
    // Rate limits by subscription tier
    private static final int FREE_TIER_LIMIT = 100;
    private static final int BASIC_TIER_LIMIT = 1000;
    private static final int PRO_TIER_LIMIT = 10000;
    
    public boolean isRateLimitExceeded(Long userId, UUID apiKeyId) {
        return isRateLimitExceeded(userId, apiKeyId, 1);
    }
    
    public boolean isRateLimitExceeded(Long userId, UUID apiKeyId, int requestCount) {
        Optional<RateLimits> rateLimitOpt = rateLimitRepository.findByUserIdAndApiKeyId(userId, apiKeyId);
        
        if (rateLimitOpt.isEmpty()) {
            RateLimits rateLimit = new RateLimits(userId, apiKeyId);
            rateLimitRepository.save(rateLimit);
            return false;
        }
        
        RateLimits rateLimit = rateLimitOpt.get();

        if (rateLimit.isCurrentlyBlocked()) {
            return true;
        }

        if (LocalDateTime.now().isAfter(rateLimit.getWindowEnd())) {
            rateLimit.resetWindow();
        }


        int currentCount = rateLimit.getRequestsCount();
        int newCount = currentCount + requestCount;
        int limit = getRateLimitForUser(userId);
        
        if (newCount > limit) {
            rateLimit.blockFor(15);
            rateLimitRepository.save(rateLimit);
            return true;
        }
        
        return false;
    }
    
    public void recordRequest(Long userId, UUID apiKeyId) {
        recordRequests(userId, apiKeyId, 1);
    }
    
    public void recordRequests(Long userId, UUID apiKeyId, int requestCount) {
        Optional<RateLimits> rateLimitOpt = rateLimitRepository.findByUserIdAndApiKeyId(userId, apiKeyId);
        
        if (rateLimitOpt.isPresent()) {
            RateLimits rateLimit = rateLimitOpt.get();

            if (LocalDateTime.now().isAfter(rateLimit.getWindowEnd())) {
                rateLimit.resetWindow();
            }
            
            for (int i = 0; i < requestCount; i++) {
                rateLimit.incrementRequests();
            }
            
            rateLimitRepository.save(rateLimit);
        }
    }
    
    public RateLimitInfo getRateLimitInfo(Long userId, UUID apiKeyId) {
        Optional<RateLimits> rateLimitOpt = rateLimitRepository.findByUserIdAndApiKeyId(userId, apiKeyId);
        
        if (rateLimitOpt.isEmpty()) {
            int limit = getRateLimitForUser(userId);
            return new RateLimitInfo(limit, 0, LocalDateTime.now().plusHours(1), false);
        }
        
        RateLimits rateLimit = rateLimitOpt.get();

        if (LocalDateTime.now().isAfter(rateLimit.getWindowEnd())) {
            rateLimit.resetWindow();
            rateLimitRepository.save(rateLimit);
        }
        
        int limit = getRateLimitForUser(userId);
        
        return new RateLimitInfo(
            limit,
            rateLimit.getRequestsCount(),
            rateLimit.getWindowEnd(),
            rateLimit.isCurrentlyBlocked()
        );
    }
    
    private int getRateLimitForUser(Long userId) {
        return FREE_TIER_LIMIT;
    }
    
    public static class RateLimitInfo {
        public final int limit;
        public final int used;
        public final LocalDateTime resetTime;
        public final boolean isBlocked;
        
        public RateLimitInfo(int limit, int used, LocalDateTime resetTime, boolean isBlocked) {
            this.limit = limit;
            this.used = used;
            this.resetTime = resetTime;
            this.isBlocked = isBlocked;
        }
    }
}