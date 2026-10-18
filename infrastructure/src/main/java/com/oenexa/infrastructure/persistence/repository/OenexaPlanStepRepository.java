package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.OenexaPlanStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OenexaPlanStepRepository extends JpaRepository<OenexaPlanStepEntity, String> {
    List<OenexaPlanStepEntity> findByGoalIdOrderByStepIndexAsc(String goalId);
}
