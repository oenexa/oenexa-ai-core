package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.OenexaSelfHealingCycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OenexaSelfHealingCycleRepository extends JpaRepository<OenexaSelfHealingCycleEntity, String> {
    List<OenexaSelfHealingCycleEntity> findByGoalIdOrderByTimestampAsc(String goalId);
}
