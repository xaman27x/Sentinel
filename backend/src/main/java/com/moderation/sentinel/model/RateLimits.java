package com.moderation.sentinel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rate_limits")
public class RateLimits {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_limit_id")
    @JsonProperty("rate_limit_id")
    private Long rateLimitId;

    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    @Column(name = "api_key_id", columnDefinition = "UUID")
    @JsonProperty("api_key_id")
    private UUID apiKeyId;

    @Column(name = "requests_count", nullable = false)
    @JsonProperty("requests_count")
    private Integer requestsCount = 0;

    @Column(name = "window_start", nullable = false)
    @JsonProperty("window_start")
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    @JsonProperty("window_end")
    private LocalDateTime windowEnd;

    @UpdateTimestamp
    @Column(name = "last_request")
    @JsonProperty("last_request")
    private LocalDateTime lastRequest;

    @Column(name = "is_blocked", nullable = false)
    @JsonProperty("is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "blocked_until")
    @JsonProperty("blocked_until")
    private LocalDateTime blockedUntil;

    // Constructors
    public RateLimits() {}

    public RateLimits(Long userId, UUID apiKeyId) {
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        this.windowStart = LocalDateTime.now();
        this.windowEnd = this.windowStart.plusHours(1); // 1-hour window
    }

    // Getters and Setters
    public Long getRateLimitId() { return rateLimitId; }
    public void setRateLimitId(Long rateLimitId) { this.rateLimitId = rateLimitId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UUID getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(UUID apiKeyId) { this.apiKeyId = apiKeyId; }

    public Integer getRequestsCount() { return requestsCount; }
    public void setRequestsCount(Integer requestsCount) { this.requestsCount = requestsCount; }

    public LocalDateTime getWindowStart() { return windowStart; }
    public void setWindowStart(LocalDateTime windowStart) { this.windowStart = windowStart; }

    public LocalDateTime getWindowEnd() { return windowEnd; }
    public void setWindowEnd(LocalDateTime windowEnd) { this.windowEnd = windowEnd; }

    public LocalDateTime getLastRequest() { return lastRequest; }
    public void setLastRequest(LocalDateTime lastRequest) { this.lastRequest = lastRequest; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }

    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }

    public void incrementRequests() {
        this.requestsCount++;
        this.lastRequest = LocalDateTime.now();
    }

    public void resetWindow() {
        this.requestsCount = 0;
        this.windowStart = LocalDateTime.now();
        this.windowEnd = this.windowStart.plusHours(1);
        this.isBlocked = false;
        this.blockedUntil = null;
    }

    public void blockFor(int minutes) {
        this.isBlocked = true;
        this.blockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    public boolean isCurrentlyBlocked() {
        return isBlocked && blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    @Override
    public String toString() {
        return "RateLimit{id=" + rateLimitId + ", requests=" + requestsCount + 
               ", blocked=" + isBlocked + "}";
    }
}