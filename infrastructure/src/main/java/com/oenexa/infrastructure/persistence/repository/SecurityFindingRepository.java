package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.SecurityFindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityFindingRepository extends JpaRepository<SecurityFindingEntity, String> {
    List<SecurityFindingEntity> findByRepositoryId(String repositoryId);
    List<SecurityFindingEntity> findBySeverity(String severity);
}
