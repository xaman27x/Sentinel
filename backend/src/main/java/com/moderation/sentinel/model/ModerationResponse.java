package com.moderation.sentinel.model;

import java.util.Map;


public class ModerationResponse {
    public final boolean isOffensive;
    public final double confidence;
    public final String message;
    public final Map<String, Double> offensiveTerms;

    public ModerationResponse(boolean isOffensive, double confidence, String message, Map<String, Double> offensiveTerms) {
        this.isOffensive = isOffensive;
        this.confidence = Math.min(1.0, Math.max(0.0, confidence)); // Clamp to [0, 1]
        this.message = message;
        this.offensiveTerms = offensiveTerms;
    }

    @Override
    public String toString() {
        return String.format("Offensive: %b, Confidence: %.2f, Message: %s, Terms: %s",
                isOffensive, confidence, message, offensiveTerms);
    }
}
