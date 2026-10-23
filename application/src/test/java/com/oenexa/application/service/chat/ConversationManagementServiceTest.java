package com.oenexa.application.service.chat;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.chat.ConversationCoreDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConversationManagementService Zero-Mock Unit Tests")
class ConversationManagementServiceTest {

    private ChatService chatService;
    private DocumentIngestionService documentIngestionService;
    private ConversationManagementService service;

    @BeforeEach
    void setUp() {
        chatService = new ChatService();
        documentIngestionService = new DocumentIngestionService();
        service = new ConversationManagementService(chatService, documentIngestionService);
    }

    @Test
    @DisplayName("Should create, list, and delete chat folders")
    void testChatFolders() {
        ChatFolder folder = service.createFolder("tenant-1", "user-1", new CreateFolderRequest("DevOps Architecture", "#10b981"));
        assertNotNull(folder.id());
        assertEquals("DevOps Architecture", folder.name());

        List<ChatFolder> folders = service.listFolders("tenant-1", "user-1");
        assertEquals(1, folders.size());

        service.assignConversationToFolder("conv-1", folder.id());
        assertEquals(folder.id(), service.getFolderForConversation("conv-1"));

        service.deleteFolder(folder.id());
        assertEquals(0, service.listFolders("tenant-1", "user-1").size());
        assertNull(service.getFolderForConversation("conv-1"));
    }

    @Test
    @DisplayName("Should pin and unpin conversations")
    void testTogglePin() {
        var meta = chatService.createConversation("conv-pin", "tenant-1", "user-1", "Kubernetes Setup", "gemini-2.5-flash");
        assertFalse(meta.pinned());

        var pinnedMeta = service.togglePin("conv-pin", true);
        assertTrue(pinnedMeta.pinned());
    }

    @Test
    @DisplayName("Should share conversation and retrieve shared view")
    void testShareConversation() {
        chatService.createConversation("conv-share", "tenant-1", "user-1", "Spring Boot Architecture", "gemini-2.5-flash");
        chatService.processMessage("conv-share", "Explain Hexagonal Architecture");

        ConversationShare share = service.shareConversation("conv-share", "user-1", "tenant-1");
        assertNotNull(share.shareToken());

        SharedConversationView view = service.getSharedConversation(share.shareToken());
        assertEquals("Spring Boot Architecture", view.title());
        assertFalse(view.messages().isEmpty());
        assertEquals(1, view.viewCount());
    }

    @Test
    @DisplayName("Should search conversations by title or message content")
    void testSearchConversations() {
        chatService.createConversation("conv-search-1", "tenant-1", "user-1", "Kafka Event Sourcing", "gemini-2.5-flash");
        chatService.processMessage("conv-search-1", "How do we handle dead letter queues?");

        var resultsByTitle = service.searchConversations("Kafka", "tenant-1");
        assertEquals(1, resultsByTitle.size());

        var resultsByContent = service.searchConversations("dead letter", "tenant-1");
        assertEquals(1, resultsByContent.size());

        var noResults = service.searchConversations("nonexistentquery123", "tenant-1");
        assertEquals(0, noResults.size());
    }

    @Test
    @DisplayName("Should add and list message reactions")
    void testMessageReactions() {
        MessageReaction r1 = service.addReaction("conv-1", 0, "user-1", "👍");
        MessageReaction r2 = service.addReaction("conv-1", 0, "user-2", "🔥");

        List<MessageReaction> reactions = service.listReactions("conv-1", 0);
        assertEquals(2, reactions.size());
        assertEquals("👍", reactions.get(0).emoji());
        assertEquals("🔥", reactions.get(1).emoji());
    }

    @Test
    @DisplayName("Should stream progressive response chunks")
    void testStreamProgressiveResponse() {
        List<ChatStreamChunk> chunks = new ArrayList<>();
        service.streamProgressiveResponse("conv-stream", "Explain REST API design", "gemini-2.5-flash", chunks::add);

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.stream().anyMatch(c -> c.delta() != null && !c.delta().isEmpty()));
        assertTrue(chunks.get(chunks.size() - 1).isFinal());
    }

    @Test
    @DisplayName("Should parse uploaded attachment through ingestion service")
    void testParseUploadedAttachment() {
        String csv = "key,val\nalpha,100\nbeta,200\n";
        ParsedDocument doc = service.parseUploadedAttachment("metrics.csv", csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(AttachmentType.CSV, doc.type());
        assertTrue(doc.extractedContent().contains("alpha"));
    }
}


