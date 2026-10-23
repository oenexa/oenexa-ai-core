package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.infrastructure.persistence.entity.UserMemoryEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Entity Mapper Layer for Persistent User Memories.
 * Translates between UserMemoryEntity JPA rows and HierarchicalMemoryRecord domain models.
 */
@Component
public class UserMemoryEntityMapper {

    public UserMemoryEntity toEntity(HierarchicalMemoryRecord record) {
        if (record == null) {
            return null;
        }
        String id = record.id() != null ? record.id() : UUID.randomUUID().toString();
        String user = record.userId() != null ? record.userId() : "default-user";
        String type = record.layer() != null ? record.layer().name() : MemoryLayerTier.L5_POSTGRESQL_RELATIONAL.name();
        String content = record.content() != null ? record.content() : "";

        return new UserMemoryEntity(
                id,
                user,
                type,
                content,
                1.0
        );
    }

    public HierarchicalMemoryRecord toDomain(UserMemoryEntity entity, String workspaceId) {
        if (entity == null) {
            return null;
        }
        MemoryLayerTier layer;
        try {
            layer = MemoryLayerTier.valueOf(entity.getMemoryType().toUpperCase());
        } catch (Exception e) {
            layer = MemoryLayerTier.L5_POSTGRESQL_RELATIONAL;
        }

        String workspace = (workspaceId != null && !workspaceId.isBlank()) ? workspaceId : "default-workspace";
        String content = entity.getContent() != null ? entity.getContent() : "";

        return new HierarchicalMemoryRecord(
                entity.getId(),
                workspace,
                entity.getUserId(),
                layer,
                "memory:" + entity.getId(),
                content,
                List.of(),
                1,
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : Instant.now(),
                content.getBytes().length,
                false
        );
    }
}
