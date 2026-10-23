package com.oenexa.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Configurations Zero-Mock Unit Tests")
class DomainConfigurationsTest {

    @Test
    @DisplayName("AuthDomainConfig provides valid properties")
    void testAuthConfig() {
        AuthDomainConfig config = new AuthDomainConfig();
        AuthDomainConfig.AuthProperties props = config.authProperties();
        assertNotNull(props);
        assertEquals(900L, props.accessTokenExpirySeconds());
        assertEquals(5, props.maxFailedAttempts());
    }

    @Test
    @DisplayName("ChatDomainConfig provides valid properties")
    void testChatConfig() {
        ChatDomainConfig config = new ChatDomainConfig();
        ChatDomainConfig.ChatProperties props = config.chatProperties();
        assertNotNull(props);
        assertEquals("gemini-2.5-flash", props.defaultModel());
        assertTrue(props.streamingEnabled());
    }

    @Test
    @DisplayName("AgentBrainConfig provides virtual thread executor and agent properties")
    void testAgentBrainConfig() {
        AgentBrainConfig config = new AgentBrainConfig();
        AgentBrainConfig.AgentProperties props = config.agentProperties();
        assertNotNull(props);
        assertEquals(16, props.maxParallelAgents());

        try (ExecutorService executor = config.agentVirtualThreadExecutor()) {
            assertNotNull(executor);
            assertFalse(executor.isShutdown());
        }
    }

    @Test
    @DisplayName("KnowledgeDomainConfig provides valid RAG properties")
    void testKnowledgeConfig() {
        KnowledgeDomainConfig config = new KnowledgeDomainConfig();
        KnowledgeDomainConfig.KnowledgeProperties props = config.knowledgeProperties();
        assertNotNull(props);
        assertEquals(500, props.maxChunkSizeChars());
        assertEquals(0.70, props.minimumSimilarityThreshold());
    }

    @Test
    @DisplayName("MemoryDomainConfig provides valid memory tier properties")
    void testMemoryConfig() {
        MemoryDomainConfig config = new MemoryDomainConfig();
        MemoryDomainConfig.MemoryProperties props = config.memoryProperties();
        assertNotNull(props);
        assertEquals(10_000, props.maxRamEntries());
        assertEquals(2048, props.maxNvmeMmapMb());
    }

    @Test
    @DisplayName("CodeDomainConfig provides valid AST and complexity bounds")
    void testCodeConfig() {
        CodeDomainConfig config = new CodeDomainConfig();
        CodeDomainConfig.CodeProperties props = config.codeProperties();
        assertNotNull(props);
        assertEquals(400, props.maxFileLines());
        assertTrue(props.zeroMockEnforced());
    }
}
