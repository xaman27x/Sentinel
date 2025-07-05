package com.moderation.sentinel.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TextModerationRequest {
    @NotBlank(message = "Text is required")
    @Size(max = 10000, message = "Text must be less than 10000 characters")
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("return_details")
    private boolean returnDetails = false;
    
    @JsonProperty("confidence_threshold")
    private Double confidenceThreshold = 0.7;
    
    public TextModerationRequest() {}
    
    public TextModerationRequest(String text) {
        this.text = text;
    }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public boolean isReturnDetails() { return returnDetails; }
    public void setReturnDetails(boolean returnDetails) { this.returnDetails = returnDetails; }
    
    public Double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(Double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
}