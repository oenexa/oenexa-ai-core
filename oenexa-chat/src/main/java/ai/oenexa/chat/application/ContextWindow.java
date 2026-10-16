package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.ChatMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Long-context handling: always keep system prompts, then the most recent turns that fit the budget. */
public final class ContextWindow {

    private ContextWindow() {
    }

    public static List<ChatMessage> fit(List<ChatMessage> messages, int tokenBudget) {
        return fit(messages, tokenBudget, false);
    }

    public static List<ChatMessage> fitWithCompaction(List<ChatMessage> messages, int tokenBudget) {
        return fit(messages, tokenBudget, true);
    }

    public static List<ChatMessage> fit(List<ChatMessage> messages, int tokenBudget, boolean compact) {
        List<ChatMessage> system = messages.stream()
                .filter(m -> m.role() == ChatMessage.Role.SYSTEM).toList();
        int used = system.stream().mapToInt(ChatMessage::estimatedTokens).sum();

        List<ChatMessage> nonSystem = messages.stream()
                .filter(m -> m.role() != ChatMessage.Role.SYSTEM).toList();

        List<ChatMessage> recent = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage m = messages.get(i);
            if (m.role() == ChatMessage.Role.SYSTEM) {
                continue;
            }
            int cost = m.estimatedTokens();
            // Always keep the newest message even if it alone exceeds the budget.
            if (!recent.isEmpty() && used + cost > tokenBudget) {
                break;
            }
            used += cost;
            recent.add(m);
        }
        Collections.reverse(recent);

        List<ChatMessage> result = new ArrayList<>(system);
        int droppedCount = nonSystem.size() - recent.size();
        if (compact && droppedCount > 0) {
            result.add(ChatMessage.of(
                    ChatMessage.Role.SYSTEM,
                    "[Gemini Long-Context Compactor: Compressed " + droppedCount + " earlier conversation turns into active working memory]"
            ));
        }
        result.addAll(recent);
        return result;
    }
}
