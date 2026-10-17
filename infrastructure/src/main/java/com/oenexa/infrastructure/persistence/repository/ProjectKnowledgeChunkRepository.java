package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ProjectKnowledgeChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectKnowledgeChunkRepository extends JpaRepository<ProjectKnowledgeChunkEntity, String> {
    List<ProjectKnowledgeChunkEntity> findByRepositoryId(String repositoryId);
    List<ProjectKnowledgeChunkEntity> findByRepositoryIdAndLanguage(String repositoryId, String language);
    void deleteByRepositoryId(String repositoryId);
}
