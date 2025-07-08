package com.moderation.sentinel.api.dto.response;

import java.util.List;

public class LogStatisticsResponse {
    private long totalRequests;
    private long offensiveCount;
    private long cleanCount;
    private double averageConfidence;
    private double averageProcessingTimeMs;
    private List<DailyUsageDto> requestsByDay;
    private List<TermCountDto> mostDetectedTerms;
    
    public LogStatisticsResponse() {
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getOffensiveCount() {
        return offensiveCount;
    }

    public void setOffensiveCount(long offensiveCount) {
        this.offensiveCount = offensiveCount;
    }

    public long getCleanCount() {
        return cleanCount;
    }

    public void setCleanCount(long cleanCount) {
        this.cleanCount = cleanCount;
    }

    public double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(double averageConfidence) {
        this.averageConfidence = averageConfidence;
    }

    public double getAverageProcessingTimeMs() {
        return averageProcessingTimeMs;
    }

    public void setAverageProcessingTimeMs(double averageProcessingTimeMs) {
        this.averageProcessingTimeMs = averageProcessingTimeMs;
    }

    public List<DailyUsageDto> getRequestsByDay() {
        return requestsByDay;
    }

    public void setRequestsByDay(List<DailyUsageDto> requestsByDay) {
        this.requestsByDay = requestsByDay;
    }

    public List<TermCountDto> getMostDetectedTerms() {
        return mostDetectedTerms;
    }

    public void setMostDetectedTerms(List<TermCountDto> mostDetectedTerms) {
        this.mostDetectedTerms = mostDetectedTerms;
    }
}