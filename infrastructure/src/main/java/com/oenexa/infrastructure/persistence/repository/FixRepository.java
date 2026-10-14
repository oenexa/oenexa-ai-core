package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.FixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixRepository extends JpaRepository<FixEntity, String> {
    List<FixEntity> findByIssueId(String issueId);
}
