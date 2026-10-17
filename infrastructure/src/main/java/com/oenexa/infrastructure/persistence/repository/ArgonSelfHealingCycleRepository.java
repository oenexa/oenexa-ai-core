package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ArgonSelfHealingCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArgonSelfHealingCycleRepository extends JpaRepository<ArgonSelfHealingCycleEntity, String> {
    List<ArgonSelfHealingCycleEntity> findByGoalIdOrderByTimestampAsc(String goalId);
    void deleteByGoalId(String goalId);
}
