package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.ChatDomain.Message;
import com.oenexa.domain.ChatDomain.OenexaAutonomousGoalResponse;
import com.oenexa.domain.ChatDomain.Role;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.RagDocument.Chunk;
import com.oenexa.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Entity Mappers Zero-Mock Unit Tests")
class EntityMappersTest {

    @Test
    @DisplayName("ChatMessageEntityMapper maps bidirectionally between JPA Entity and Domain Message")
    void testChatMessageEntityMapper() {
        ChatMessageEntityMapper mapper = new ChatMessageEntityMapper();
        assertNull(mapper.toEntity(null, "s1"));
        assertNull(mapper.toDomain(null));

        Message domain = new Message(Role.USER, "Show git status", Instant.now());
        ChatMessageEntity entity = mapper.toEntity(domain, "session-123");
        assertNotNull(entity);
        assertEquals("USER", entity.getRole());
        assertEquals("Show git status", entity.getContent());
        assertEquals("session-123", entity.getSessionId());

        Message roundTrip = mapper.toDomain(entity);
        assertNotNull(roundTrip);
        assertEquals(Role.USER, roundTrip.role());
        assertEquals("Show git status", roundTrip.content());
    }

    @Test
    @DisplayName("OenexaGoalEntityMapper maps bidirectionally between OenexaGoalEntity and Domain Goal Response")
    void testOenexaGoalEntityMapper() {
        OenexaGoalEntityMapper mapper = new OenexaGoalEntityMapper();
        assertNull(mapper.toDomain(null));

        OenexaGoalEntity entity = mapper.toEntity("Implement Zero-Mock tests", "u1", "repo-1");
        assertNotNull(entity);
        assertEquals("Implement Zero-Mock tests", entity.getGoalDescription());

        OenexaAutonomousGoalResponse response = mapper.toDomain(entity);
        assertNotNull(response);
        assertEquals(entity.getId(), response.goalId());
        assertEquals("Implement Zero-Mock tests", response.goalDescription());
    }

    @Test
    @DisplayName("KnowledgeChunkEntityMapper maps bidirectionally between Entity and Chunk record")
    void testKnowledgeChunkEntityMapper() {
        KnowledgeChunkEntityMapper mapper = new KnowledgeChunkEntityMapper();
        assertNull(mapper.toEntity(null, "r1", "p1", "md"));
        assertNull(mapper.toDomain(null));

        Chunk chunk = new Chunk("c-1", "Architecture", "Hexagonal Monolith Details", 0);
        ProjectKnowledgeChunkEntity entity = mapper.toEntity(chunk, "repo-10", "docs/ARCH.md", "markdown");
        assertNotNull(entity);
        assertEquals("c-1", entity.getId());
        assertEquals("Hexagonal Monolith Details", entity.getContent());

        Chunk domainChunk = mapper.toDomain(entity);
        assertNotNull(domainChunk);
        assertEquals("c-1", domainChunk.id());
        assertEquals("Hexagonal Monolith Details", domainChunk.text());
    }

    @Test
    @DisplayName("UserMemoryEntityMapper maps bidirectionally between UserMemoryEntity and HierarchicalMemoryRecord")
    void testUserMemoryEntityMapper() {
        UserMemoryEntityMapper mapper = new UserMemoryEntityMapper();
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toDomain(null, "ws-1"));

        HierarchicalMemoryRecord record = new HierarchicalMemoryRecord(
                "mem-99", "ws-alpha", "user-42",
                MemoryLayerTier.L3_REDIS_DISTRIBUTED, "user:pref",
                "Dark theme enabled", List.of(), 1, Instant.now(), 64, false
        );

        UserMemoryEntity entity = mapper.toEntity(record);
        assertNotNull(entity);
        assertEquals("L3_REDIS_DISTRIBUTED", entity.getMemoryType());
        assertEquals("Dark theme enabled", entity.getContent());

        HierarchicalMemoryRecord roundTrip = mapper.toDomain(entity, "ws-alpha");
        assertNotNull(roundTrip);
        assertEquals(MemoryLayerTier.L3_REDIS_DISTRIBUTED, roundTrip.layer());
        assertEquals("Dark theme enabled", roundTrip.content());
    }

    @Test
    @DisplayName("RepositoryEntityMapper maps RepositoryEntity to domain summary")
    void testRepositoryEntityMapper() {
        RepositoryEntityMapper mapper = new RepositoryEntityMapper();
        assertNull(mapper.toDomainSummary(null, 5));

        RepositoryEntity entity = mapper.toEntity("proj-1", "oenexa-core", "https://github.com/oenexa/core", "main");
        assertNotNull(entity);
        assertEquals("oenexa-core", entity.getName());

        CodeAnalysisResponse summary = mapper.toDomainSummary(entity, 10);
        assertNotNull(summary);
        assertEquals(10, summary.classCount());
        assertTrue(summary.clean());
    }
}
