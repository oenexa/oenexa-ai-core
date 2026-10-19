package ai.oenexa.chat.domain;

public record CodeContextAttachment(
        String filePath,
        String language,
        int startLine,
        int endLine,
        String content
) {
    public CodeContextAttachment(String filePath, String content) {
        this(filePath, "plaintext", 1, 1, content);
    }
}
