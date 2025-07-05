package com.moderation.sentinel.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchModerationRequest {
    @NotNull(message = "Texts array is required")
    @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100")
    @JsonProperty("texts")
    private List<String> texts;
    
    @JsonProperty("return_details")
    private boolean returnDetails = false;
    
    @JsonProperty("confidence_threshold")
    private Double confidenceThreshold = 0.7;
    
    public BatchModerationRequest() {}
    
    public BatchModerationRequest(List<String> texts) {
        this.texts = texts;
    }
    
    public List<String> getTexts() { return texts; }
    public void setTexts(List<String> texts) { this.texts = texts; }
    
    public boolean isReturnDetails() { return returnDetails; }
    public void setReturnDetails(boolean returnDetails) { this.returnDetails = returnDetails; }
    
    public Double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(Double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
}