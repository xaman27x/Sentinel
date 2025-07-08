package com.moderation.sentinel.service.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moderation.sentinel.api.dto.response.DailyUsageDto;
import com.moderation.sentinel.api.dto.response.LogStatisticsResponse;
import com.moderation.sentinel.api.dto.response.TermCountDto;
import com.moderation.sentinel.model.ModerationLogs;
import com.moderation.sentinel.repository.ApiKeyRepository;
import com.moderation.sentinel.repository.ModerationLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModerationLogsService {

    @Autowired
    private ModerationLogsRepository logsRepository;
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public Page<ModerationLogs> getUserLogs(Long userId, Pageable pageable) {
        return logsRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Page<ModerationLogs> getApiKeyLogs(UUID apiKeyId, Pageable pageable) {
        return logsRepository.findByApiKeyIdOrderByCreatedAtDesc(apiKeyId, pageable);
    }
    
    public ModerationLogs getLogById(Long logId) {
        return logsRepository.findById(logId).orElse(null);
    }
    
    public boolean validateApiKeyOwnership(Long userId, UUID apiKeyId) {
        return apiKeyRepository.existsByUserIdAndApiKeyId(userId, apiKeyId);
    }
    
    public LogStatisticsResponse getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        LogStatisticsResponse statistics = new LogStatisticsResponse();
        
        // Total requests
        long totalRequests = logsRepository.countByUserIdAndCreatedAtBetween(userId, startDate, endDate);
        statistics.setTotalRequests(totalRequests);
        
        // Offensive content count
        long offensiveCount = logsRepository.countByUserIdAndIsOffensiveAndCreatedAtBetween(
            userId, true, startDate, endDate);
        statistics.setOffensiveCount(offensiveCount);
        
        // Clean content count
        statistics.setCleanCount(totalRequests - offensiveCount);
        
        // Average confidence
        Double avgConfidence = logsRepository.getAverageConfidenceScoreByUserIdAndPeriod(
            userId, startDate, endDate);
        statistics.setAverageConfidence(avgConfidence != null ? avgConfidence : 0.0);
        
        // Average processing time
        Double avgProcessingTime = logsRepository.getAverageProcessingTimeByUserId(userId, startDate, endDate);
        statistics.setAverageProcessingTimeMs(avgProcessingTime != null ? avgProcessingTime : 0.0);
        
        // Requests by day
        List<Object[]> dailyData = logsRepository.countRequestsByDay(userId, startDate, endDate);
        List<DailyUsageDto> requestsByDay = new ArrayList<>();
        
        for (Object[] row : dailyData) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Long count = (Long) row[1];
            Long offensiveCountForDay = (Long) row[2];
            
            requestsByDay.add(new DailyUsageDto(
                date.toString(),
                count,
                offensiveCountForDay
            ));
        }
        statistics.setRequestsByDay(requestsByDay);
        
        // Most detected terms
        List<ModerationLogs> offensiveLogs = logsRepository.findByUserIdAndIsOffensiveAndCreatedAtBetween(
            userId, true, startDate, endDate);
        
        Map<String, Integer> termCounts = new HashMap<>();
        
        for (ModerationLogs log : offensiveLogs) {
            try {
                Map<String, Double> detectedTerms = objectMapper.readValue(
                    log.getDetectedTerms(), 
                    new TypeReference<Map<String, Double>>() {}
                );
                
                for (String term : detectedTerms.keySet()) {
                    termCounts.put(term, termCounts.getOrDefault(term, 0) + 1);
                }
            } catch (JsonProcessingException e) {
                // Skip if we can't parse the JSON
            }
        }
        
        List<TermCountDto> mostDetectedTerms = termCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(entry -> new TermCountDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
        
        statistics.setMostDetectedTerms(mostDetectedTerms);
        
        return statistics;
    }
}