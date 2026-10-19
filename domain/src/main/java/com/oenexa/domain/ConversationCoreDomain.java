package com.oenexa.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Phase 2 - OENEXA AI Core Domain Models.
 * Provides contracts for Chat Folders, Pinned Chats, Shared Conversations,
 * Message Reactions, Multi-Format File Attachments, and Streaming Response Chunks.
 */
public class ConversationCoreDomain {

    public enum AttachmentType {
        PDF, DOCX, TXT, XLSX, CSV, JSON, XML, IMAGE, ZIP, CODE
    }

    public record ChatFolder(
            String id,
            String name,
            String color,
            String tenantId,
            String userId,
            Instant createdAt
    ) {
        public ChatFolder(String name, String color, String tenantId, String userId) {
            this(UUID.randomUUID().toString(), name, color != null ? color : "#6366f1", tenantId, userId, Instant.now());
        }
    }

    public record CreateFolderRequest(String name, String color) {}

    public record AssignFolderRequest(String conversationId, String folderId) {}

    public record ConversationShare(
            String shareToken,
            String conversationId,
            String sharedByUserId,
            String tenantId,
            Instant sharedAt,
            boolean isPublic,
            int viewCount
    ) {
        public ConversationShare(String conversationId, String sharedByUserId, String tenantId) {
            this(UUID.randomUUID().toString().replace("-", ""), conversationId, sharedByUserId, tenantId, Instant.now(), true, 0);
        }
    }

    public record MessageReaction(
            String reactionId,
            String conversationId,
            int messageIndex,
            String userId,
            String emoji,
            Instant reactedAt
    ) {
        public MessageReaction(String conversationId, int messageIndex, String userId, String emoji) {
            this(UUID.randomUUID().toString(), conversationId, messageIndex, userId, emoji, Instant.now());
        }
    }

    public record MessageReactionRequest(String emoji) {}

    public record ParsedDocument(
            String documentId,
            String fileName,
            AttachmentType type,
            long sizeBytes,
            String mimeType,
            String extractedContent,
            String summary,
            int tokenCount,
            List<String> detectedEntities,
            Instant parsedAt
    ) {
        public ParsedDocument(String fileName, AttachmentType type, long sizeBytes, String extractedContent, String summary) {
            this(
                    UUID.randomUUID().toString(),
                    fileName,
                    type,
                    sizeBytes,
                    resolveMimeType(type),
                    extractedContent,
                    summary,
                    Math.max(1, extractedContent.length() / 4),
                    List.of(),
                    Instant.now()
            );
        }

        private static String resolveMimeType(AttachmentType type) {
            return switch (type) {
                case PDF -> "application/pdf";
                case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case CSV -> "text/csv";
                case JSON -> "application/json";
                case XML -> "application/xml";
                case IMAGE -> "image/png";
                case ZIP -> "application/zip";
                case TXT, CODE -> "text/plain";
            };
        }
    }

    public record ChatStreamChunk(
            String conversationId,
            int sequence,
            String delta,
            String thoughtDelta,
            boolean isFinal,
            Instant timestamp
    ) {
        public static ChatStreamChunk text(String conversationId, int sequence, String delta) {
            return new ChatStreamChunk(conversationId, sequence, delta, null, false, Instant.now());
        }

        public static ChatStreamChunk thought(String conversationId, int sequence, String thoughtDelta) {
            return new ChatStreamChunk(conversationId, sequence, null, thoughtDelta, false, Instant.now());
        }

        public static ChatStreamChunk complete(String conversationId, int sequence) {
            return new ChatStreamChunk(conversationId, sequence, "", null, true, Instant.now());
        }
    }

    public record SharedConversationView(
            String shareToken,
            String title,
            String model,
            Instant createdAt,
            List<ChatDomain.Message> messages,
            int viewCount
    ) {}
}
