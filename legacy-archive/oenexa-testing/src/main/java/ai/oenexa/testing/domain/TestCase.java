package ai.oenexa.testing.domain;

import java.util.Objects;

/**
 * An individual synthesized test case specification.
 */
public record TestCase(
        String id,
        String name,
        String givenWhenThen,
        String testCode,
        boolean assertionsIncluded
) {
    public TestCase {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (givenWhenThen == null) {
            givenWhenThen = "";
        }
        if (testCode == null) {
            testCode = "";
        }
    }
}
