package ai.oenexa.chat.application;

import ai.oenexa.chat.domain.CodeArtifact;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts structured CodeArtifacts from Markdown code blocks in AI responses.
 */
public class ArtifactExtractor {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([a-zA-Z0-9_-]*)\\s*\\n([\\s\\S]*?)```");
    private static final Pattern FILE_HINT_PATTERN = Pattern.compile("(?i)(?://|#|<!--)\\s*(?:file|path):\\s*([^\\n]+)");

    public List<CodeArtifact> extract(String responseText) {
        if (responseText == null || responseText.isBlank()) {
            return List.of();
        }

        List<CodeArtifact> artifacts = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(responseText);

        int counter = 1;
        while (matcher.find()) {
            String language = matcher.group(1);
            if (language == null || language.isBlank()) {
                language = "plaintext";
            } else {
                language = language.trim().toLowerCase();
            }

            String code = matcher.group(2).trim();
            if (code.isBlank()) {
                continue;
            }

            // Look for file hint
            String filePath;
            Matcher fileMatcher = FILE_HINT_PATTERN.matcher(code);
            if (fileMatcher.find()) {
                filePath = fileMatcher.group(1).trim();
            } else {
                filePath = defaultFilePath(language, counter);
            }

            String title = (filePath.contains("/"))
                    ? filePath.substring(filePath.lastIndexOf('/') + 1)
                    : filePath;

            String action = (code.startsWith("--- ") || code.startsWith("+++ ") || code.contains("@@ -"))
                    ? "PATCH"
                    : "CREATE";

            artifacts.add(new CodeArtifact(
                    UUID.randomUUID().toString(),
                    title,
                    filePath,
                    language,
                    code,
                    action
            ));
            counter++;
        }

        return List.copyOf(artifacts);
    }

    private String defaultFilePath(String language, int index) {
        return switch (language) {
            case "java" -> "src/main/java/Component" + index + ".java";
            case "typescript", "ts" -> "src/component" + index + ".ts";
            case "javascript", "js" -> "src/index" + index + ".js";
            case "json" -> "config" + index + ".json";
            case "yaml", "yml" -> "manifest" + index + ".yml";
            case "mermaid" -> "architecture" + index + ".mmd";
            case "sql" -> "migration" + index + ".sql";
            case "dockerfile" -> "Dockerfile";
            default -> "snippet" + index + ".txt";
        };
    }
}
