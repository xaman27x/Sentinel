package com.moderation.sentinel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "api_key_id", columnDefinition = "UUID")
    private UUID apiKeyId;

    @Column(name = "user_id", nullable = false)
    @JsonProperty("user_id")
    private Long userId;

    @Column(name = "api_key", nullable = false, unique = true)
    @JsonProperty("api_key")
    private String apiKey;

    @Column(name = "key_name")
    @JsonProperty("key_name")
    private String keyName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column(name = "revoked_at")
    @JsonProperty("revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "is_active", nullable = false)
    @JsonProperty("is_active")
    private Boolean isActive = true;

    @Column(name = "last_used")
    @JsonProperty("last_used")
    private LocalDateTime lastUsed;

    @Column(name = "usage_count", nullable = false)
    @JsonProperty("usage_count")
    private Long usageCount = 0L;

    // Constructors
    public ApiKey() {}

    public ApiKey(Long userId, String apiKey, String keyName) {
        this.userId = userId;
        this.apiKey = apiKey;
        this.keyName = keyName;
    }

    // Getters and Setters
    public UUID getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(UUID apiKeyId) { this.apiKeyId = apiKeyId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }

    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }

    public void incrementUsage() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
    }

    public void revoke() {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ApiKey{id=" + apiKeyId + ", name='" + keyName + "', active=" + isActive + "}";
    }
}