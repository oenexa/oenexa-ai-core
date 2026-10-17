package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ArgonGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArgonGoalRepository extends JpaRepository<ArgonGoalEntity, String> {
    List<ArgonGoalEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<ArgonGoalEntity> findByStatus(String status);
}
