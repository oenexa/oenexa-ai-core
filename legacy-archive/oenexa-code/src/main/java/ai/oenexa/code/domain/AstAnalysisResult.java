package ai.oenexa.code.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result of static AST analysis on a code snippet.
 */
public record AstAnalysisResult(
        String language,
        int totalLines,
        int classCount,
        int methodCount,
        int cyclomaticComplexity,
        List<String> imports,
        List<String> detectedCodeSmells,
        List<AstNode> topLevelNodes
) {
    public AstAnalysisResult {
        Objects.requireNonNull(language, "language must not be null");
        if (imports == null) imports = List.of();
        if (detectedCodeSmells == null) detectedCodeSmells = List.of();
        if (topLevelNodes == null) topLevelNodes = List.of();
    }

    public boolean isClean() {
        return detectedCodeSmells.isEmpty() && cyclomaticComplexity <= 10;
    }
}
