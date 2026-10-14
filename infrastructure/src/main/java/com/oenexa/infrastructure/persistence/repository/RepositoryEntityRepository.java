package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositoryEntityRepository extends JpaRepository<RepositoryEntity, String> {
    List<RepositoryEntity> findByProjectId(String projectId);
}
