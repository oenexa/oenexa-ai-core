package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.MultimodalAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultimodalAttachmentRepository extends JpaRepository<MultimodalAttachmentEntity, String> {
    List<MultimodalAttachmentEntity> findByGoalIdOrderByUploadedAtAsc(String goalId);
}
