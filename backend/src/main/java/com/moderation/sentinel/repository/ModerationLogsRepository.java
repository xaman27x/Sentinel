package com.moderation.sentinel.repository;

import com.moderation.sentinel.model.ModerationLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModerationLogsRepository extends JpaRepository<ModerationLogs, Long> {
    
    List<ModerationLogs> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<ModerationLogs> findByApiKeyIdOrderByCreatedAtDesc(UUID apiKeyId);
    
    List<ModerationLogs> findByIsOffensiveOrderByCreatedAtDesc(Boolean isOffensive);
    
    @Query("SELECT ml FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt >= :startDate")
    List<ModerationLogs> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ml FROM ModerationLogs ml WHERE ml.apiKeyId = :apiKeyId AND ml.createdAt >= :startDate")
    List<ModerationLogs> findByApiKeyIdAndCreatedAtAfter(@Param("apiKeyId") UUID apiKeyId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(ml) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt >= :startDate")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(ml.confidenceScore) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.isOffensive = true")
    Double getAverageConfidenceScoreByUserId(@Param("userId") Long userId);
}