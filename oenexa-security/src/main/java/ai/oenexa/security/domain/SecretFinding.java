package ai.oenexa.security.domain;

import java.util.Objects;

/**
 * Finding produced by secrets detection engine.
 */
public record SecretFinding(
        SecretType type,
        String maskedValue,
        String file,
        int line
) {
    public enum SecretType {
        AWS_ACCESS_KEY,
        GITHUB_TOKEN,
        PRIVATE_KEY,
        JWT_SECRET,
        GENERIC_API_KEY
    }

    public SecretFinding {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(maskedValue, "maskedValue must not be null");
        if (file == null) file = "unknown";
    }

    public static String mask(String raw) {
        if (raw == null || raw.length() <= 6) {
            return "******";
        }
        return raw.substring(0, 3) + "..." + raw.substring(raw.length() - 3);
    }
}
