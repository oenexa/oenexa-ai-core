package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanRepository extends JpaRepository<ScanEntity, String> {
    List<ScanEntity> findByRepositoryId(String repositoryId);
}
