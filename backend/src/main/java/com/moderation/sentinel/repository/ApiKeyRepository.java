package com.moderation.sentinel.repository;

import com.moderation.sentinel.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    
    Optional<ApiKey> findByApiKeyAndIsActiveTrue(String apiKey);
    
    List<ApiKey> findByUserIdAndIsActiveTrue(Long userId);
    
    List<ApiKey> findByUserId(Long userId);
    
    boolean existsByApiKey(String apiKey);
    
    @Query("SELECT COUNT(ak) FROM ApiKey ak WHERE ak.userId = :userId AND ak.isActive = true")
    long countActiveKeysByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.lastUsed < :cutoffDate AND ak.isActive = true")
    List<ApiKey> findInactiveKeys(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT ak FROM ApiKey ak WHERE ak.userId = :userId ORDER BY ak.createdAt DESC")
    List<ApiKey> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}