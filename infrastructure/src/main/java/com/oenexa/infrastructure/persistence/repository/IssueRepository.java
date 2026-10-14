package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.IssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<IssueEntity, String> {
    List<IssueEntity> findByRepositoryId(String repositoryId);
    List<IssueEntity> findByStatus(String status);
}
