package com.oenexa.domain.model.chat;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Platform Chat & Core Contracts Zero-Mock Unit Tests")
class PlatformChatAndCoreDomainTest {

    @Test
    void testChatDomainRecordsAndInstantiation() {
        assertNotNull(new ChatDomain());
        assertEquals(4, ChatDomain.Role.values().length);
        assertEquals(ChatDomain.Role.TOOL, ChatDomain.Role.valueOf("TOOL"));

        Instant now = Instant.now();
        ChatDomain.Message message = new ChatDomain.Message(ChatDomain.Role.USER, "Hello AI", now);
        assertEquals(ChatDomain.Role.USER, message.role());
        assertEquals("Hello AI", message.content());
        assertEquals(now, message.timestamp());
        assertNull(message.thoughtProcess());
        assertEquals("gemini-2.5-flash", message.model());
        assertTrue(message.attachments().isEmpty());
        assertTrue(message.toolCalls().isEmpty());
        assertTrue(message.toolResults().isEmpty());
        assertTrue(message.artifacts().isEmpty());

        ChatDomain.Message advancedMsg = new ChatDomain.Message(
                ChatDomain.Role.ASSISTANT,
                "Answer",
                now,
                "Thought step",
                "gemini-2.5-pro"
        );
        assertEquals("Thought step", advancedMsg.thoughtProcess());
        assertEquals("gemini-2.5-pro", advancedMsg.model());

        ChatDomain.Conversation conv = new ChatDomain.Conversation("c1", "u1", List.of(message));
        assertEquals("c1", conv.conversationId());
        assertEquals("u1", conv.userId());
        assertEquals(1, conv.messages().size());

        ChatDomain.ChatRequest request = new ChatDomain.ChatRequest("Help me build", "conv-1");
        assertEquals("Help me build", request.message());
        assertEquals("conv-1", request.conversationId());
        assertEquals("gemini-2.5-flash", request.model());
        assertTrue(request.includeThinking());
        assertTrue(request.attachments().isEmpty());

        ChatDomain.ChatRequest advancedReq = new ChatDomain.ChatRequest("query", "c1", "gemini-2.5-pro", false);
        assertEquals("gemini-2.5-pro", advancedReq.model());
        assertFalse(advancedReq.includeThinking());

        ChatDomain.ChatResponse resp = new ChatDomain.ChatResponse("reply", "c1", 42);
        assertEquals("reply", resp.reply());
        assertEquals("c1", resp.conversationId());
        assertEquals(42, resp.contextTokensUsed());
        assertNull(resp.thoughtProcess());
        assertEquals("gemini-2.5-flash", resp.model());
        assertEquals("GENERAL_ASSISTANT", resp.intent());
        assertTrue(resp.suggestedFollowUps().isEmpty());
        assertTrue(resp.toolCalls().isEmpty());
        assertTrue(resp.artifacts().isEmpty());
        assertEquals(0.95, resp.confidenceScore());

        ChatDomain.ChatResponse advancedResp = new ChatDomain.ChatResponse(
                "reply",
                "c1",
                42,
                "Thought",
                "gemini-2.5-pro",
                "ARCHITECTURE_DESIGN",
                List.of("Follow up 1")
        );
        assertEquals("Thought", advancedResp.thoughtProcess());
        assertEquals("gemini-2.5-pro", advancedResp.model());
        assertEquals("ARCHITECTURE_DESIGN", advancedResp.intent());
        assertEquals(1, advancedResp.suggestedFollowUps().size());

        ChatDomain.ToolCall toolCall = new ChatDomain.ToolCall("call-1", "ast_analyzer");
        assertEquals("call-1", toolCall.callId());
        assertEquals("ast_analyzer", toolCall.toolName());
        assertTrue(toolCall.arguments().isEmpty());

        ChatDomain.ToolCall toolCallWithArgs = new ChatDomain.ToolCall("call-2", "security_scan", java.util.Map.of("target", "src/"));
        assertEquals("src/", toolCallWithArgs.arguments().get("target"));

        ChatDomain.ToolResult toolResult = new ChatDomain.ToolResult("call-1", "ast_analyzer", "OK");
        assertEquals("call-1", toolResult.callId());
        assertEquals("ast_analyzer", toolResult.toolName());
        assertEquals("OK", toolResult.output());
        assertFalse(toolResult.isError());

        ChatDomain.ToolResult toolResultError = new ChatDomain.ToolResult("call-2", "security_scan", "Failed", true);
        assertTrue(toolResultError.isError());

        ChatDomain.CodeContextAttachment attachment = new ChatDomain.CodeContextAttachment("src/Main.java", "public class Main {}");
        assertEquals("src/Main.java", attachment.filePath());
        assertEquals("public class Main {}", attachment.content());
        assertEquals(1, attachment.startLine());

        ChatDomain.CodeContextAttachment fullAttachment = new ChatDomain.CodeContextAttachment("src/App.java", "java", 10, 20, "void run() {}");
        assertEquals("java", fullAttachment.language());
        assertEquals(10, fullAttachment.startLine());
        assertEquals(20, fullAttachment.endLine());

        ChatDomain.CodeArtifact artifact = new ChatDomain.CodeArtifact("Main Class", "src/Main.java", "class Main {}");
        assertEquals("Main Class", artifact.title());
        assertEquals("src/Main.java", artifact.filePath());
        assertEquals("CREATE", artifact.action());
        assertNotNull(artifact.artifactId());

        ChatDomain.CodeArtifact customArtifact = new ChatDomain.CodeArtifact("a-1", "Diff", "src/App.java", "diff", "+ line", "PATCH");
        assertEquals("a-1", customArtifact.artifactId());
        assertEquals("PATCH", customArtifact.action());

        ChatDomain.ConversationMetadata meta = new ChatDomain.ConversationMetadata("c-100", "New Chat Session");
        assertEquals("c-100", meta.conversationId());
        assertEquals("New Chat Session", meta.title());
        assertFalse(meta.pinned());
        assertEquals(0, meta.messageCount());

        ChatDomain.ConversationMetadata fullMeta = new ChatDomain.ConversationMetadata(
                "c-200", "tenant-1", "user-1", "Architecture Review", "gemini-2.5-pro",
                now, now, 5, true
        );
        assertEquals("tenant-1", fullMeta.tenantId());
        assertEquals("user-1", fullMeta.userId());
        assertTrue(fullMeta.pinned());
        assertEquals(5, fullMeta.messageCount());

        ChatDomain.Message proMsg = new ChatDomain.Message(
                ChatDomain.Role.TOOL,
                "Tool output",
                now,
                "Thinking",
                "gemini-2.5-pro",
                List.of(attachment),
                List.of(toolCall),
                List.of(toolResult),
                List.of(artifact)
        );
        assertEquals(ChatDomain.Role.TOOL, proMsg.role());
        assertEquals(1, proMsg.attachments().size());
        assertEquals(1, proMsg.toolCalls().size());
        assertEquals(1, proMsg.toolResults().size());
        assertEquals(1, proMsg.artifacts().size());

        ChatDomain.ChatRequest proReq = new ChatDomain.ChatRequest("Analyze", "c-100", "gemini-2.5-pro", true, List.of(attachment));
        assertEquals(1, proReq.attachments().size());

        ChatDomain.ChatResponse proResp = new ChatDomain.ChatResponse(
                "Completed", "c-100", 150, "Thought", "gemini-2.5-pro", "CODE_GENERATION",
                List.of("Run tests"), List.of(toolCall), List.of(artifact), 0.98
        );
        assertEquals(0.98, proResp.confidenceScore());
        assertEquals(1, proResp.toolCalls().size());
        assertEquals(1, proResp.artifacts().size());
    }

    @Test
    void testDomainEvent() {
        DomainEvent.StandardDomainEvent event = DomainEvent.StandardDomainEvent.of("USER_CREATED", "tenant-1", "payload-data");
        assertNotNull(event.eventId());
        assertEquals("USER_CREATED", event.type());
        assertEquals("tenant-1", event.tenantId());
        assertNotNull(event.occurredAt());
        assertEquals("payload-data", event.payload());

        DomainEvent custom = new DomainEvent() {
            private final UUID id = UUID.randomUUID();
            private final Instant time = Instant.now();

            @Override
            public UUID eventId() { return id; }
            @Override
            public String type() { return "CUSTOM"; }
            @Override
            public String tenantId() { return "tenant-x"; }
            @Override
            public Instant occurredAt() { return time; }
        };
        assertNotNull(custom.eventId());
        assertEquals("CUSTOM", custom.type());
        assertEquals("tenant-x", custom.tenantId());
        assertNotNull(custom.occurredAt());
    }

    @Test
    void testErrorCode() {
        assertEquals(400, ErrorCode.VALIDATION.httpStatus());
        assertEquals(401, ErrorCode.UNAUTHORIZED.httpStatus());
        assertEquals(403, ErrorCode.FORBIDDEN.httpStatus());
        assertEquals(404, ErrorCode.NOT_FOUND.httpStatus());
        assertEquals(409, ErrorCode.CONFLICT.httpStatus());
        assertEquals(429, ErrorCode.RATE_LIMITED.httpStatus());
        assertEquals(502, ErrorCode.UPSTREAM_FAILURE.httpStatus());
        assertEquals(500, ErrorCode.INTERNAL.httpStatus());
    }

    @Test
    void testOenexaException() {
        OenexaException ex = new OenexaException(ErrorCode.NOT_FOUND, "Entity missing");
        assertEquals(ErrorCode.NOT_FOUND, ex.code());
        assertEquals("Entity missing", ex.getMessage());

        Throwable cause = new IllegalArgumentException("invalid id");
        OenexaException exWithCause = new OenexaException(ErrorCode.VALIDATION, "Validation failed", cause);
        assertEquals(ErrorCode.VALIDATION, exWithCause.code());
        assertEquals("Validation failed", exWithCause.getMessage());
        assertEquals(cause, exWithCause.getCause());
    }

    @Test
    void testRagDocumentRecordsAndInstantiation() {
        assertNotNull(new RagDocument());

        RagDocument.DocumentInput input = new RagDocument.DocumentInput("Doc", "Text", "repo-1");
        assertEquals("Doc", input.title());
        assertEquals("Text", input.content());
        assertEquals("repo-1", input.repositoryId());

        RagDocument.Chunk chunk = new RagDocument.Chunk("c-1", "Doc", "Snippet", 0);
        assertEquals("c-1", chunk.id());
        assertEquals("Doc", chunk.title());
        assertEquals("Snippet", chunk.text());
        assertEquals(0, chunk.chunkIndex());

        RagDocument.SearchResult search = new RagDocument.SearchResult("c-1", "Doc", "Snippet", 0.95);
        assertEquals("c-1", search.chunkId());
        assertEquals("Doc", search.title());
        assertEquals("Snippet", search.snippet());
        assertEquals(0.95, search.similarityScore());

        RagDocument.IngestResult ingest = new RagDocument.IngestResult(2, List.of("c-1", "c-2"));
        assertEquals(2, ingest.chunksIndexed());
        assertEquals(List.of("c-1", "c-2"), ingest.chunkIds());
    }
}

