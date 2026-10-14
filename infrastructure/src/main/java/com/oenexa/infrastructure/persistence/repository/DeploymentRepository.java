package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.DeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentRepository extends JpaRepository<DeploymentEntity, String> {
    List<DeploymentEntity> findByProjectId(String projectId);
}
