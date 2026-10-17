package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise AI Memory Service managing Short-Term sliding windows,
 * Long-Term user preferences/facts, and automated context summarization.
 */
@Service
public class MemoryService {

    private static final int MAX_SHORT_TERM_MESSAGES = 50;

    // In-memory sliding window cache for active session messages
    private final Map<String, List<Message>> shortTermCache = new ConcurrentHashMap<>();

    // In-memory user memories indexed by userId
    private final Map<String, List<UserMemoryRecord>> userMemoryStore = new ConcurrentHashMap<>();

    // Summaries per session
    private final Map<String, String> sessionSummaries = new ConcurrentHashMap<>();

    public void addMessageToShortTerm(String sessionId, Message message) {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        Objects.requireNonNull(message, "message must not be null");

        List<Message> messages = shortTermCache.computeIfAbsent(sessionId, k -> new ArrayList<>());
        synchronized (messages) {
            messages.add(message);
            if (messages.size() > MAX_SHORT_TERM_MESSAGES) {
                messages.remove(0);
            }
        }
    }

    public List<Message> getShortTermMemory(String sessionId) {
        List<Message> list = shortTermCache.get(sessionId);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    public List<String> getShortTermMemorySnippets(String sessionId) {
        List<Message> list = getShortTermMemory(sessionId);
        if (list.isEmpty()) {
            return List.of();
        }
        List<String> snippets = new ArrayList<>();
        int start = Math.max(0, list.size() - 5);
        for (int i = start; i < list.size(); i++) {
            Message m = list.get(i);
            String snippet = m.role() + ": " + (m.content().length() > 80 ? m.content().substring(0, 80) + "..." : m.content());
            snippets.add(snippet);
        }
        return snippets;
    }

    public String generateContextSummary(String sessionId) {
        List<Message> list = getShortTermMemory(sessionId);
        if (list.size() < 4) {
            return sessionSummaries.getOrDefault(sessionId, "Conversation initialized; context window within optimal range.");
        }
        StringBuilder sb = new StringBuilder("Summary of ").append(list.size()).append(" turns: ");
        int userCount = 0;
        for (Message m : list) {
            if (m.role() == Role.USER) {
                userCount++;
                if (userCount <= 3) {
                    sb.append(" [Topic: ").append(m.content().length() > 40 ? m.content().substring(0, 40) + "..." : m.content()).append("]");
                }
            }
        }
        String summary = sb.toString();
        sessionSummaries.put(sessionId, summary);
        return summary;
    }

    public void recordUserMemory(UserMemoryRecord record) {
        Objects.requireNonNull(record, "record must not be null");
        userMemoryStore.computeIfAbsent(record.userId(), k -> new ArrayList<>()).add(record);
    }

    public List<UserMemoryRecord> getUserMemories(String userId) {
        return List.copyOf(userMemoryStore.getOrDefault(userId, List.of()));
    }

    public List<UserMemoryRecord> searchUserMemories(String userId, String query) {
        List<UserMemoryRecord> all = getUserMemories(userId);
        if (query == null || query.isBlank() || all.isEmpty()) {
            return all;
        }
        String q = query.toLowerCase();
        return all.stream()
                .filter(m -> m.memoryKey().toLowerCase().contains(q) || m.memoryValue().toLowerCase().contains(q))
                .toList();
    }

    public Optional<UserMemoryRecord> learnFromUserTurn(String userId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return Optional.empty();
        }
        String lower = messageText.toLowerCase();
        if (lower.contains("prefer") || lower.contains("we use") || lower.contains("our architecture") || lower.contains("always use")) {
            String key = "DEVELOPER_CONVENTION";
            String cat = "PREFERENCE";
            if (lower.contains("architecture")) {
                key = "ARCHITECTURE_STYLE";
                cat = "PROJECT_CONTEXT";
            } else if (lower.contains("database") || lower.contains("postgres") || lower.contains("redis")) {
                key = "PERSISTENCE_STACK";
                cat = "FACT";
            }
            UserMemoryRecord mem = new UserMemoryRecord(
                    UUID.randomUUID().toString(),
                    userId,
                    "default-tenant",
                    key,
                    messageText.trim(),
                    cat,
                    0.96,
                    Instant.now()
            );
            recordUserMemory(mem);
            return Optional.of(mem);
        }
        return Optional.empty();
    }

    public String generateSessionTitle(String firstMessage) {
        if (firstMessage == null || firstMessage.isBlank()) {
            return "New AI Conversation";
        }
        String clean = firstMessage.replaceAll("[\\r\\n]+", " ").trim();
        if (clean.length() <= 40) {
            return clean;
        }
        return clean.substring(0, 37) + "...";
    }

    public ConversationCategory categorizeConversation(String text) {
        if (text == null) return ConversationCategory.GENERAL;
        String lower = text.toLowerCase();
        if (lower.contains("architecture") || lower.contains("hexagonal") || lower.contains("monolith") || lower.contains("microservice")) {
            return ConversationCategory.ARCHITECTURE;
        } else if (lower.contains("bug") || lower.contains("fix") || lower.contains("exception") || lower.contains("npe") || lower.contains("error")) {
            return ConversationCategory.BUG_FIXING;
        } else if (lower.contains("test") || lower.contains("junit") || lower.contains("coverage") || lower.contains("zero-mock")) {
            return ConversationCategory.TESTING;
        } else if (lower.contains("docker") || lower.contains("k8s") || lower.contains("kubernetes") || lower.contains("terraform") || lower.contains("ci/cd")) {
            return ConversationCategory.DEVOPS;
        } else if (lower.contains("security") || lower.contains("sast") || lower.contains("vulnerability") || lower.contains("cve") || lower.contains("owasp")) {
            return ConversationCategory.SECURITY;
        } else if (lower.contains("controller") || lower.contains("service") || lower.contains("entity") || lower.contains("api") || lower.contains("code") || lower.contains("refactor")) {
            return ConversationCategory.CODE_ENGINEERING;
        }
        return ConversationCategory.GENERAL;
    }
}
