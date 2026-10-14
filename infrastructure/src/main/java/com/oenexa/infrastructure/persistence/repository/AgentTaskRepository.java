package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.AgentTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentTaskRepository extends JpaRepository<AgentTaskEntity, String> {
    List<AgentTaskEntity> findByStatus(String status);
    List<AgentTaskEntity> findByAgentType(String agentType);
}
