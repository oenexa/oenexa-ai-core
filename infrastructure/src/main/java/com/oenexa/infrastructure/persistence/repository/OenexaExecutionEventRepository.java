package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.OenexaExecutionEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OenexaExecutionEventRepository extends JpaRepository<OenexaExecutionEventEntity, String> {
    List<OenexaExecutionEventEntity> findByGoalIdOrderByTimestampAsc(String goalId);
}
