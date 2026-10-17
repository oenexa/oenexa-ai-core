package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ArgonPlanStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArgonPlanStepRepository extends JpaRepository<ArgonPlanStepEntity, String> {
    List<ArgonPlanStepEntity> findByGoalIdOrderByStepIndexAsc(String goalId);
    void deleteByGoalId(String goalId);
}
