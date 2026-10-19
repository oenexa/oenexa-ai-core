package com.oenexa.domain;

import com.oenexa.domain.ConversationCoreDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConversationCoreDomain Zero-Mock Unit Tests")
class ConversationCoreDomainTest {

    @Test
    @DisplayName("Should create and validate ChatFolder invariants")
    void testChatFolder() {
        ChatFolder folder = new ChatFolder("Backend Architecture", "#4f46e5", "tenant-1", "user-1");
        assertNotNull(folder.id());
        assertEquals("Backend Architecture", folder.name());
        assertEquals("#4f46e5", folder.color());
        assertEquals("tenant-1", folder.tenantId());
        assertEquals("user-1", folder.userId());
        assertNotNull(folder.createdAt());
    }

    @Test
    @DisplayName("Should create ConversationShare and verify token generation")
    void testConversationShare() {
        ConversationShare share = new ConversationShare("conv-123", "user-1", "tenant-1");
        assertNotNull(share.shareToken());
        assertTrue(share.shareToken().length() >= 16);
        assertEquals("conv-123", share.conversationId());
        assertTrue(share.isPublic());
        assertEquals(0, share.viewCount());
    }

    @Test
    @DisplayName("Should create MessageReaction record")
    void testMessageReaction() {
        MessageReaction reaction = new MessageReaction("conv-123", 2, "user-1", "👍");
        assertNotNull(reaction.reactionId());
        assertEquals("conv-123", reaction.conversationId());
        assertEquals(2, reaction.messageIndex());
        assertEquals("👍", reaction.emoji());
    }

    @Test
    @DisplayName("Should parse and calculate token count for ParsedDocument")
    void testParsedDocument() {
        String text = "Public class Solution { void run() {} }";
        ParsedDocument doc = new ParsedDocument("Solution.java", AttachmentType.CODE, 1024, text, "Java solution code");
        assertNotNull(doc.documentId());
        assertEquals("Solution.java", doc.fileName());
        assertEquals(AttachmentType.CODE, doc.type());
        assertEquals("text/plain", doc.mimeType());
        assertTrue(doc.tokenCount() > 0);
        assertEquals(text, doc.extractedContent());
    }

    @Test
    @DisplayName("Should build streaming chunks correctly")
    void testChatStreamChunks() {
        ChatStreamChunk thought = ChatStreamChunk.thought("conv-1", 1, "Analyzing request...");
        assertFalse(thought.isFinal());
        assertEquals("Analyzing request...", thought.thoughtDelta());
        assertNull(thought.delta());

        ChatStreamChunk text = ChatStreamChunk.text("conv-1", 2, "Here is the response");
        assertFalse(text.isFinal());
        assertEquals("Here is the response", text.delta());

        ChatStreamChunk complete = ChatStreamChunk.complete("conv-1", 3);
        assertTrue(complete.isFinal());
    }
}
