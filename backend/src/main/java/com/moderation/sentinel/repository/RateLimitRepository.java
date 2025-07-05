package com.moderation.sentinel.repository;

import com.moderation.sentinel.model.RateLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateLimitRepository extends JpaRepository<RateLimits, Long> {
    
    Optional<RateLimits> findByUserIdAndApiKeyId(Long userId, UUID apiKeyId);
    
    Optional<RateLimits> findByUserId(Long userId);
    
    Optional<RateLimits> findByApiKeyId(UUID apiKeyId);
    
    @Query("SELECT rl FROM RateLimits rl WHERE rl.isBlocked = true AND rl.blockedUntil < :currentTime")
    List<RateLimits> findExpiredBlocks(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT rl FROM RateLimits rl WHERE rl.windowEnd < :currentTime")
    List<RateLimits> findExpiredWindows(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(rl) FROM RateLimits rl WHERE rl.userId = :userId AND rl.windowStart >= :startTime")
    long countRequestsByUserSince(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}