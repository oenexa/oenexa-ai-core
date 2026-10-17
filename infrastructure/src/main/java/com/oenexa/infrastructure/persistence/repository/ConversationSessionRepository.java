package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ConversationSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSessionEntity, String> {
    List<ConversationSessionEntity> findByUserIdOrderByUpdatedAtDesc(String userId);
    List<ConversationSessionEntity> findByUserIdAndCategoryOrderByUpdatedAtDesc(String userId, String category);
}
