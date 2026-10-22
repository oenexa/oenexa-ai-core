package com.oenexa.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Conversation & Memory JPA Entities Zero-Mock Invariant & Mutator Tests")
class ConversationEntityModelTest {

    @Test
    @DisplayName("ConversationSessionEntity getters, setters, and defaults")
    void testConversationSessionEntity() {
        ConversationSessionEntity session = new ConversationSessionEntity("sess-1", "user-1", "Build Auth Service", "CODE_ENGINEERING", "ANTHROPIC_CLAUDE");
        ConversationSessionEntity noArg = new ConversationSessionEntity();
        assertNotNull(noArg);

        assertEquals("sess-1", session.getId());
        assertEquals("user-1", session.getUserId());
        assertEquals("Build Auth Service", session.getTitle());
        assertEquals("CODE_ENGINEERING", session.getCategory());
        assertEquals("ANTHROPIC_CLAUDE", session.getModelProvider());
        assertEquals(0, session.getMessageCount());
        assertNull(session.getSummary());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getUpdatedAt());

        session.setId("sess-updated");
        session.setUserId("user-2");
        session.setTitle("New Title");
        session.setCategory("ARCHITECTURE");
        session.setSummary("Architecture review summary");
        session.setModelProvider("GEMINI");
        session.setMessageCount(15);
        Instant now = Instant.now();
        session.setCreatedAt(now);
        session.setUpdatedAt(now);

        assertEquals("sess-updated", session.getId());
        assertEquals("user-2", session.getUserId());
        assertEquals("New Title", session.getTitle());
        assertEquals("ARCHITECTURE", session.getCategory());
        assertEquals("Architecture review summary", session.getSummary());
        assertEquals("GEMINI", session.getModelProvider());
        assertEquals(15, session.getMessageCount());
        assertEquals(now, session.getCreatedAt());
        assertEquals(now, session.getUpdatedAt());

        ConversationSessionEntity defaultSession = new ConversationSessionEntity("sess-2", "user-1", "Default", null, null);
        assertEquals("GENERAL", defaultSession.getCategory());
        assertEquals("GEMINI", defaultSession.getModelProvider());
    }

    @Test
    @DisplayName("ChatMessageEntity getters and setters")
    void testChatMessageEntity() {
        ChatMessageEntity msg = new ChatMessageEntity("msg-1", "sess-1", "USER", "How to configure Kafka?", 12, "OPENAI_GPT", "CODE_AGENT", 0.95);
        ChatMessageEntity noArg = new ChatMessageEntity();
        assertNotNull(noArg);

        assertEquals("msg-1", msg.getId());
        assertEquals("sess-1", msg.getSessionId());
        assertEquals("USER", msg.getRole());
        assertEquals("How to configure Kafka?", msg.getContent());
        assertEquals(12, msg.getTokenCount());
        assertEquals("OPENAI_GPT", msg.getModelUsed());
        assertEquals("CODE_AGENT", msg.getAgentType());
        assertEquals(0.95, msg.getRankingScore(), 0.001);
        assertNotNull(msg.getCreatedAt());

        msg.setId("msg-2");
        msg.setSessionId("sess-2");
        msg.setRole("ASSISTANT");
        msg.setContent("Config steps...");
        msg.setTokenCount(250);
        msg.setModelUsed("ANTHROPIC_CLAUDE");
        msg.setAgentType("DEVOPS_AGENT");
        msg.setRankingScore(0.99);
        Instant now = Instant.now();
        msg.setCreatedAt(now);

        assertEquals("msg-2", msg.getId());
        assertEquals("sess-2", msg.getSessionId());
        assertEquals("ASSISTANT", msg.getRole());
        assertEquals("Config steps...", msg.getContent());
        assertEquals(250, msg.getTokenCount());
        assertEquals("ANTHROPIC_CLAUDE", msg.getModelUsed());
        assertEquals("DEVOPS_AGENT", msg.getAgentType());
        assertEquals(0.99, msg.getRankingScore(), 0.001);
        assertEquals(now, msg.getCreatedAt());
    }

    @Test
    @DisplayName("UserMemoryEntity getters and setters")
    void testUserMemoryEntity() {
        UserMemoryEntity mem = new UserMemoryEntity("mem-1", "user-1", "PREFERENCE", "Prefers Hexagonal Architecture", 0.98);
        UserMemoryEntity noArg = new UserMemoryEntity();
        assertNotNull(noArg);

        assertEquals("mem-1", mem.getId());
        assertEquals("user-1", mem.getUserId());
        assertEquals("PREFERENCE", mem.getMemoryType());
        assertEquals("Prefers Hexagonal Architecture", mem.getContent());
        assertEquals(0.98, mem.getConfidenceScore(), 0.001);
        assertNotNull(mem.getCreatedAt());
        assertNotNull(mem.getUpdatedAt());

        mem.setId("mem-2");
        mem.setUserId("user-2");
        mem.setMemoryType("FACT");
        mem.setContent("Uses PostgreSQL 16");
        mem.setConfidenceScore(1.0);
        Instant now = Instant.now();
        mem.setCreatedAt(now);
        mem.setUpdatedAt(now);

        assertEquals("mem-2", mem.getId());
        assertEquals("user-2", mem.getUserId());
        assertEquals("FACT", mem.getMemoryType());
        assertEquals("Uses PostgreSQL 16", mem.getContent());
        assertEquals(1.0, mem.getConfidenceScore(), 0.001);
        assertEquals(now, mem.getCreatedAt());
        assertEquals(now, mem.getUpdatedAt());
    }

    @Test
    @DisplayName("ProjectKnowledgeChunkEntity getters, setters, and defaults")
    void testProjectKnowledgeChunkEntity() {
        ProjectKnowledgeChunkEntity chunk = new ProjectKnowledgeChunkEntity(
                "chunk-1", "repo-1", "src/AuthService.java", "JAVA",
                "CLASS", "AuthService", "public class AuthService {}", "[0.12, 0.45, 0.88]", 1
        );
        ProjectKnowledgeChunkEntity noArg = new ProjectKnowledgeChunkEntity();
        assertNotNull(noArg);

        assertEquals("chunk-1", chunk.getId());
        assertEquals("repo-1", chunk.getRepositoryId());
        assertEquals("src/AuthService.java", chunk.getFilePath());
        assertEquals("JAVA", chunk.getLanguage());
        assertEquals("CLASS", chunk.getSymbolType());
        assertEquals("AuthService", chunk.getSymbolName());
        assertEquals("public class AuthService {}", chunk.getContent());
        assertEquals("[0.12, 0.45, 0.88]", chunk.getEmbeddingVectorJson());
        assertEquals(1, chunk.getChunkIndex());
        assertNotNull(chunk.getCreatedAt());

        chunk.setId("chunk-2");
        chunk.setRepositoryId("repo-2");
        chunk.setFilePath("src/App.tsx");
        chunk.setLanguage("TYPESCRIPT");
        chunk.setSymbolType("COMPONENT");
        chunk.setSymbolName("App");
        chunk.setContent("export default function App() {}");
        chunk.setEmbeddingVectorJson("[0.99, 0.01]");
        chunk.setChunkIndex(2);
        Instant now = Instant.now();
        chunk.setCreatedAt(now);

        assertEquals("chunk-2", chunk.getId());
        assertEquals("repo-2", chunk.getRepositoryId());
        assertEquals("src/App.tsx", chunk.getFilePath());
        assertEquals("TYPESCRIPT", chunk.getLanguage());
        assertEquals("COMPONENT", chunk.getSymbolType());
        assertEquals("App", chunk.getSymbolName());
        assertEquals("export default function App() {}", chunk.getContent());
        assertEquals("[0.99, 0.01]", chunk.getEmbeddingVectorJson());
        assertEquals(2, chunk.getChunkIndex());
        assertEquals(now, chunk.getCreatedAt());

        ProjectKnowledgeChunkEntity defaultChunk = new ProjectKnowledgeChunkEntity(
                "chunk-3", "repo-1", "Dockerfile", "DOCKERFILE", "CONFIG", "Docker", "FROM eclipse-temurin:21", null, 0
        );
        assertEquals("[]", defaultChunk.getEmbeddingVectorJson());
    }
}
