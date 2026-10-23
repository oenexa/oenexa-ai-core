package com.oenexa.application.mapper;

import com.oenexa.domain.model.auth.AuthDomain.AuthClaims;
import com.oenexa.domain.model.auth.AuthDomain.UserProfile;
import com.oenexa.domain.model.chat.ChatDomain.*;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.model.memory.EnterpriseMemoryHierarchyDomain.MemoryRetrievalResult;
import com.oenexa.domain.model.knowledge.RagDocument.Chunk;
import com.oenexa.domain.model.knowledge.RagDocument.DocumentInput;
import com.oenexa.domain.model.knowledge.RagDocument.SearchResult;
import com.oenexa.domain.model.auth.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application DTO Mappers Zero-Mock Unit Tests")
class ApplicationMappersTest {

    @Test
    @DisplayName("AuthDtoMapper maps User to UserProfile and AuthClaims correctly")
    void shouldMapUserToProfileAndClaims() {
        AuthDtoMapper mapper = new AuthDtoMapper();
        assertNull(mapper.toProfile(null));
        assertNull(mapper.toClaims(null, 3600));

        User user = new User("developer", "hash123", "dev@oenexa.ai").withId(42L);
        UserProfile profile = mapper.toProfile(user);
        assertNotNull(profile);
        assertEquals(42L, profile.id());
        assertEquals("developer", profile.username());
        assertEquals("dev@oenexa.ai", profile.email());

        AuthClaims claims = mapper.toClaims(user, 3600);
        assertNotNull(claims);
        assertEquals("42", claims.userId());
        assertEquals("developer", claims.username());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("ChatDtoMapper maps Message and ChatResponse correctly")
    void shouldMapChatMessage() {
        ChatDtoMapper mapper = new ChatDtoMapper();
        assertNull(mapper.toResponse(null, "c1", 10));

        Message msg = new Message(Role.ASSISTANT, "Hello world", Instant.now(), "think", "gemini-2.5-flash");
        ChatResponse resp = mapper.toResponse(msg, "conv-99", 50);
        assertNotNull(resp);
        assertEquals("Hello world", resp.reply());
        assertEquals("conv-99", resp.conversationId());
        assertEquals(50, resp.contextTokensUsed());

        Message assistantMsg = mapper.toAssistantMessage("Direct reply", "thinking...", "gemini-2.5-flash");
        assertNotNull(assistantMsg);
        assertEquals(Role.ASSISTANT, assistantMsg.role());
        assertEquals("Direct reply", assistantMsg.content());
    }

    @Test
    @DisplayName("AgentGoalDtoMapper maps autonomous goal requests into response envelopes")
    void shouldMapAgentGoal() {
        AgentGoalDtoMapper mapper = new AgentGoalDtoMapper();
        assertNull(mapper.toCompletedResponse(null, "g1", null, "summary"));

        OenexaAutonomousGoalRequest req = new OenexaAutonomousGoalRequest("Design microservices");
        OenexaAutonomousGoalResponse resp = mapper.toCompletedResponse(req, "goal-1", null, "Done");
        assertNotNull(resp);
        assertEquals("goal-1", resp.goalId());
        assertEquals("Design microservices", resp.goalDescription());
        assertEquals("Done", resp.finalSummary());
    }

    @Test
    @DisplayName("KnowledgeDtoMapper chunks document input and maps search results")
    void shouldChunkAndMapKnowledge() {
        KnowledgeDtoMapper mapper = new KnowledgeDtoMapper();
        assertTrue(mapper.toChunks(null, 100).isEmpty());

        DocumentInput input = new DocumentInput("Arch", "A".repeat(120), "repo-1");
        List<Chunk> chunks = mapper.toChunks(input, 50);
        assertEquals(3, chunks.size());

        SearchResult search = mapper.toSearchResult(chunks.get(0), 0.95);
        assertNotNull(search);
        assertEquals(0.95, search.similarityScore());
    }

    @Test
    @DisplayName("MemoryDtoMapper maps HierarchicalMemoryRecord to MemoryRetrievalResult")
    void shouldMapMemoryRecord() {
        MemoryDtoMapper mapper = new MemoryDtoMapper();
        MemoryRetrievalResult nullResult = mapper.toRetrievalResult(null, MemoryLayerTier.L2_JVM_HEAP_RAM, 100);
        assertFalse(nullResult.hit());

        HierarchicalMemoryRecord record = new HierarchicalMemoryRecord(
                "m-1", "w-1", "u-1", MemoryLayerTier.L3_REDIS_DISTRIBUTED,
                "session:token", "jwt-payload", List.of(), 5, Instant.now(), 120, false
        );
        MemoryRetrievalResult hit = mapper.toRetrievalResult(record, MemoryLayerTier.L3_REDIS_DISTRIBUTED, 50);
        assertTrue(hit.hit());
        assertEquals("session:token", hit.key());
        assertEquals(MemoryLayerTier.L3_REDIS_DISTRIBUTED, hit.sourceTier());
    }

    @Test
    @DisplayName("CodeDtoMapper maps analysis and security scan payloads")
    void shouldMapCodePayloads() {
        CodeDtoMapper mapper = new CodeDtoMapper();
        CodeAnalysisResponse analysis = mapper.toAnalysisResponse("java", 150, 2, 8, List.of("A", "B"), List.of("m1"), List.of("java.util.*"));
        assertNotNull(analysis);
        assertEquals(150, analysis.totalLines());
        assertTrue(analysis.clean());

        SecurityScanResponse sec = mapper.toSecurityResponse(true, 0, List.of(), List.of(), "Clean");
        assertNotNull(sec);
        assertTrue(sec.passed());
        assertEquals(0, sec.totalIssues());
    }
}

