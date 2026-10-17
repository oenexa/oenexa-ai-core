package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.UserMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMemoryRepository extends JpaRepository<UserMemoryEntity, String> {
    List<UserMemoryEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<UserMemoryEntity> findByUserIdAndMemoryTypeOrderByConfidenceScoreDesc(String userId, String memoryType);
    void deleteByUserId(String userId);
}
