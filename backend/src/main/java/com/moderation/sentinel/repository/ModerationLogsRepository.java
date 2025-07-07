package com.moderation.sentinel.repository;

import com.moderation.sentinel.model.ModerationLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModerationLogsRepository extends JpaRepository<ModerationLogs, Long> {

    
    Page<ModerationLogs> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    
    Page<ModerationLogs> findByApiKeyIdOrderByCreatedAtDesc(UUID apiKeyId, Pageable pageable);
    
    List<ModerationLogs> findByIsOffensiveOrderByCreatedAtDesc(Boolean isOffensive);
    
    // Period-based Queries
    @Query("SELECT ml FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt BETWEEN :startDate AND :endDate ORDER BY ml.createdAt DESC")
    List<ModerationLogs> findByUserIdAndCreatedAtBetween(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ml FROM ModerationLogs ml WHERE ml.apiKeyId = :apiKeyId AND ml.createdAt BETWEEN :startDate AND :endDate ORDER BY ml.createdAt DESC")
    List<ModerationLogs> findByApiKeyIdAndCreatedAtBetween(
        @Param("apiKeyId") UUID apiKeyId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count Queries
    @Query("SELECT COUNT(ml) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt BETWEEN :startDate AND :endDate")
    long countByUserIdAndCreatedAtBetween(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(ml) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.isOffensive = :isOffensive AND ml.createdAt BETWEEN :startDate AND :endDate")
    long countByUserIdAndIsOffensiveAndCreatedAtBetween(
        @Param("userId") Long userId,
        @Param("isOffensive") boolean isOffensive,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Specific Retrieval Queries
    @Query("SELECT ml FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.isOffensive = :isOffensive AND ml.createdAt BETWEEN :startDate AND :endDate")
    List<ModerationLogs> findByUserIdAndIsOffensiveAndCreatedAtBetween(
        @Param("userId") Long userId,
        @Param("isOffensive") boolean isOffensive,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // Statistics Queries
    @Query("SELECT AVG(ml.confidenceScore) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageConfidenceScoreByUserIdAndPeriod(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT AVG(ml.processingTimeMs) FROM ModerationLogs ml WHERE ml.userId = :userId AND ml.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageProcessingTimeByUserId(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query(value = 
        "SELECT DATE(created_at) as date, COUNT(*) as count, " +
        "SUM(CASE WHEN is_offensive = true THEN 1 ELSE 0 END) as offensive_count " +
        "FROM moderation_logs " +
        "WHERE user_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
        "GROUP BY DATE(created_at) " +
        "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> countRequestsByDay(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}