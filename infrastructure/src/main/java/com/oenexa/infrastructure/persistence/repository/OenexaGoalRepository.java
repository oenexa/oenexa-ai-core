package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.OenexaGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OenexaGoalRepository extends JpaRepository<OenexaGoalEntity, String> {
    List<OenexaGoalEntity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<OenexaGoalEntity> findByRepositoryIdOrderByCreatedAtDesc(String repositoryId);
}
