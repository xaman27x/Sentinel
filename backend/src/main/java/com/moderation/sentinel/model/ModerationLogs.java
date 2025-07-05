package com.moderation.sentinel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "moderation_logs")
public class ModerationLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    @JsonProperty("log_id")
    private Long logId;

    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    @Column(name = "api_key_id", columnDefinition = "UUID")
    @JsonProperty("api_key_id")
    private UUID apiKeyId;

    @Column(name = "input_text", columnDefinition = "TEXT")
    @JsonProperty("input_text")
    private String inputText;

    @Column(name = "is_offensive", nullable = false)
    @JsonProperty("is_offensive")
    private Boolean isOffensive;

    @Column(name = "confidence_score", nullable = false)
    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @Column(name = "detected_terms", columnDefinition = "TEXT")
    @JsonProperty("detected_terms")
    private String detectedTerms;

    @Column(name = "processing_time_ms")
    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Column(name = "client_ip")
    @JsonProperty("client_ip")
    private String clientIp;

    @Column(name = "user_agent")
    @JsonProperty("user_agent")
    private String userAgent;

    // Constructors
    public ModerationLogs() {}

    public ModerationLogs(Long userId, UUID apiKeyId, String inputText, Boolean isOffensive,
                         Double confidenceScore, String detectedTerms) {
        this.userId = userId;
        this.apiKeyId = apiKeyId;
        this.inputText = inputText;
        this.isOffensive = isOffensive;
        this.confidenceScore = confidenceScore;
        this.detectedTerms = detectedTerms;
    }

    // Getters and Setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UUID getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(UUID apiKeyId) { this.apiKeyId = apiKeyId; }

    public String getInputText() { return inputText; }
    public void setInputText(String inputText) { this.inputText = inputText; }

    public Boolean getIsOffensive() { return isOffensive; }
    public void setIsOffensive(Boolean isOffensive) { this.isOffensive = isOffensive; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getDetectedTerms() { return detectedTerms; }
    public void setDetectedTerms(String detectedTerms) { this.detectedTerms = detectedTerms; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    @Override
    public String toString() {
        return "ModerationLog{id=" + logId + ", offensive=" + isOffensive + 
               ", confidence=" + confidenceScore + "}";
    }
}