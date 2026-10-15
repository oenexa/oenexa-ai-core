package ai.oenexa.code.domain;

import java.util.List;
import java.util.Objects;

/**
 * Node in a parsed Abstract Syntax Tree representation.
 */
public record AstNode(
        String nodeType,
        String name,
        int startLine,
        int endLine,
        List<AstNode> children
) {
    public AstNode {
        Objects.requireNonNull(nodeType, "nodeType must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (children == null) {
            children = List.of();
        }
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
