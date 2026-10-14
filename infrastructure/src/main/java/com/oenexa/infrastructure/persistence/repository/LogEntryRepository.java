package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.LogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntryEntity, String> {
    List<LogEntryEntity> findByRepositoryId(String repositoryId);
}
