package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.LocalAgentAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalAgentAuditLogRepository extends JpaRepository<LocalAgentAuditLogEntity, String> {
    List<LocalAgentAuditLogEntity> findByGoalIdOrderByCreatedAtDesc(String goalId);
    Optional<LocalAgentAuditLogEntity> findTopByOrderByCreatedAtDesc();
}
