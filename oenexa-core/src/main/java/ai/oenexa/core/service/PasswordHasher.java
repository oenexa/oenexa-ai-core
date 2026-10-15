package ai.oenexa.core.service;

@FunctionalInterface
public interface PasswordHasher {
    String hash(String rawPassword);

    default boolean verify(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return hash(rawPassword).equals(hashedPassword);
    }
}
