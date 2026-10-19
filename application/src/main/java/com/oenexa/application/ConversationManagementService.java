package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import com.oenexa.domain.ConversationCoreDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Enterprise Conversation Management Service.
 * Why: Orchestrates Chat Folders, Pinned Chats, Shared Conversations,
 * Message Reactions, Full-Text Chat Search, and Streaming progressive tokens.
 */
@Service
public class ConversationManagementService {

    private final ChatService chatService;
    private final DocumentIngestionService documentIngestionService;

    private final Map<String, ChatFolder> folders = new ConcurrentHashMap<>();
    private final Map<String, String> conversationFolderMap = new ConcurrentHashMap<>();
    private final Map<String, ConversationShare> shares = new ConcurrentHashMap<>();
    private final Map<String, List<MessageReaction>> reactions = new ConcurrentHashMap<>();

    @Autowired
    public ConversationManagementService(ChatService chatService, DocumentIngestionService documentIngestionService) {
        this.chatService = Objects.requireNonNull(chatService, "chatService must not be null");
        this.documentIngestionService = Objects.requireNonNull(documentIngestionService, "documentIngestionService must not be null");
    }

    public ChatFolder createFolder(String tenantId, String userId, CreateFolderRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        String name = (request.name() != null && !request.name().isBlank()) ? request.name() : "New Folder";
        ChatFolder folder = new ChatFolder(name, request.color(), tenantId, userId);
        folders.put(folder.id(), folder);
        return folder;
    }

    public List<ChatFolder> listFolders(String tenantId, String userId) {
        List<ChatFolder> list = new ArrayList<>(folders.values());
        list.sort(Comparator.comparing(ChatFolder::createdAt).reversed());
        return List.copyOf(list);
    }

    public void deleteFolder(String folderId) {
        folders.remove(folderId);
        conversationFolderMap.values().removeIf(id -> id.equals(folderId));
    }

    public void assignConversationToFolder(String conversationId, String folderId) {
        if (folderId == null || folderId.isBlank()) {
            conversationFolderMap.remove(conversationId);
        } else {
            conversationFolderMap.put(conversationId, folderId);
        }
    }

    public String getFolderForConversation(String conversationId) {
        return conversationFolderMap.get(conversationId);
    }

    public ConversationMetadata togglePin(String conversationId, boolean pinned) {
        ConversationMetadata existing = chatService.getMetadata(conversationId);
        ConversationMetadata updated = new ConversationMetadata(
                existing.conversationId(),
                existing.tenantId(),
                existing.userId(),
                existing.title(),
                existing.model(),
                existing.createdAt(),
                Instant.now(),
                existing.messageCount(),
                pinned
        );
        return updated;
    }

    public ConversationShare shareConversation(String conversationId, String userId, String tenantId) {
        ConversationShare share = new ConversationShare(conversationId, userId, tenantId);
        shares.put(share.shareToken(), share);
        return share;
    }

    public SharedConversationView getSharedConversation(String shareToken) {
        ConversationShare share = shares.get(shareToken);
        if (share == null) {
            throw new NoSuchElementException("Shared conversation not found or expired: " + shareToken);
        }
        shares.put(shareToken, new ConversationShare(
                share.shareToken(),
                share.conversationId(),
                share.sharedByUserId(),
                share.tenantId(),
                share.sharedAt(),
                share.isPublic(),
                share.viewCount() + 1
        ));

        ConversationMetadata meta = chatService.getMetadata(share.conversationId());
        List<Message> history = chatService.getHistory(share.conversationId());
        return new SharedConversationView(
                share.shareToken(),
                meta.title(),
                meta.model(),
                meta.createdAt(),
                history,
                share.viewCount() + 1
        );
    }

    public List<ConversationMetadata> searchConversations(String query, String tenantId) {
        if (query == null || query.isBlank()) {
            return chatService.listConversations(tenantId);
        }
        String needle = query.toLowerCase(Locale.ROOT);
        List<ConversationMetadata> matches = new ArrayList<>();
        for (ConversationMetadata meta : chatService.listConversations(tenantId)) {
            if (meta.title().toLowerCase(Locale.ROOT).contains(needle)) {
                matches.add(meta);
                continue;
            }
            List<Message> messages = chatService.getHistory(meta.conversationId());
            boolean contentMatch = messages.stream().anyMatch(m -> m.content().toLowerCase(Locale.ROOT).contains(needle));
            if (contentMatch) {
                matches.add(meta);
            }
        }
        return List.copyOf(matches);
    }

    public MessageReaction addReaction(String conversationId, int messageIndex, String userId, String emoji) {
        String key = conversationId + ":" + messageIndex;
        MessageReaction reaction = new MessageReaction(conversationId, messageIndex, userId, emoji);
        reactions.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).add(reaction);
        return reaction;
    }

    public List<MessageReaction> listReactions(String conversationId, int messageIndex) {
        String key = conversationId + ":" + messageIndex;
        return List.copyOf(reactions.getOrDefault(key, List.of()));
    }

    public ParsedDocument parseUploadedAttachment(String fileName, byte[] bytes) {
        return documentIngestionService.parse(fileName, bytes);
    }

    public void streamProgressiveResponse(
            String conversationId,
            String messageText,
            String model,
            Consumer<ChatStreamChunk> chunkConsumer
    ) {
        Objects.requireNonNull(chunkConsumer, "chunkConsumer must not be null");
        ChatResponse resp = chatService.processMessage(conversationId, messageText, model, true, List.of());

        int seq = 1;
        if (resp.thoughtProcess() != null && !resp.thoughtProcess().isBlank()) {
            String[] thoughtParts = resp.thoughtProcess().split("\n");
            for (String part : thoughtParts) {
                chunkConsumer.accept(ChatStreamChunk.thought(conversationId, seq++, part + "\n"));
            }
        }

        String reply = resp.reply();
        int chunkSize = 25;
        for (int i = 0; i < reply.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, reply.length());
            chunkConsumer.accept(ChatStreamChunk.text(conversationId, seq++, reply.substring(i, end)));
        }

        chunkConsumer.accept(ChatStreamChunk.complete(conversationId, seq));
    }
}
