package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.TestExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecutionEntity, String> {
    List<TestExecutionEntity> findByRepositoryId(String repositoryId);
}
