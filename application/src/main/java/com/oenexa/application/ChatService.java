package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Interactive Chat Application Service.
 * Why: Context window management and intelligent code engineering assistance.
 */
@Service
public class ChatService {

    private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    public ChatResponse processMessage(String conversationId, String messageText) {
        Objects.requireNonNull(messageText, "messageText must not be null");

        String convId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : UUID.randomUUID().toString();

        List<Message> history = conversations.computeIfAbsent(convId, k -> new ArrayList<>());

        // Append user message
        history.add(new Message(Role.USER, messageText, Instant.now()));

        // Formulate assistant response based on software engineering persona
        String reply = generateEngineeringResponse(messageText);
        history.add(new Message(Role.ASSISTANT, reply, Instant.now()));

        int estimatedTokens = (messageText.length() + reply.length()) / 4;
        return new ChatResponse(reply, convId, estimatedTokens);
    }

    public List<Message> getHistory(String conversationId) {
        return List.copyOf(conversations.getOrDefault(conversationId, List.of()));
    }

    private String generateEngineeringResponse(String input) {
        String lower = input.toLowerCase();
        if (lower.contains("architecture") || lower.contains("design")) {
            return "OENEXA Architecture: Polyrepo / Hexagonal modular monolith. Clean separation of domain, application, infrastructure, and api tiers.";
        } else if (lower.contains("scan") || lower.contains("security")) {
            return "Security Agent activated: Scanning for OWASP Top 10, credential leakage, and dependencies vulnerabilities.";
        } else if (lower.contains("fix") || lower.contains("bug")) {
            return "Bug Fix Agent activated: Analyzing stack trace and synthesizing minimal non-breaking patch.";
        }
        return "OENEXA AI Engineer: Request received. Ready to assist with coding, testing, architecture, and deployment.";
    }
}
