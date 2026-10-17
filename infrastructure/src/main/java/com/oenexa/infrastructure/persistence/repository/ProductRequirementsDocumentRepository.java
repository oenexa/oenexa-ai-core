package com.oenexa.infrastructure.persistence.repository;

import com.oenexa.infrastructure.persistence.entity.ProductRequirementsDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRequirementsDocumentRepository extends JpaRepository<ProductRequirementsDocumentEntity, String> {
    List<ProductRequirementsDocumentEntity> findByOrderByCreatedAtDesc();
}
