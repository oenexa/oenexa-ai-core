package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ArgonExecutionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArgonExecutionEventRepository extends JpaRepository<ArgonExecutionEventEntity, String> {
    List<ArgonExecutionEventEntity> findByGoalIdOrderByTimestampAsc(String goalId);
    void deleteByGoalId(String goalId);
}
