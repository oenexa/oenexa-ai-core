package ai.oenexa.chat.domain;

import java.util.UUID;

public record CodeArtifact(
        String artifactId,
        String title,
        String filePath,
        String language,
        String codeContent,
        String action
) {
    public CodeArtifact(String title, String filePath, String codeContent) {
        this(UUID.randomUUID().toString(), title, filePath, "java", codeContent, "CREATE");
    }
}
