package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.UserStoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStoryRepository extends JpaRepository<UserStoryEntity, String> {
    List<UserStoryEntity> findByPrdIdOrderByCreatedAtAsc(String prdId);
    List<UserStoryEntity> findByStatus(String status);
}
