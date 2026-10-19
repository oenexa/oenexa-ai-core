package ai.oenexa.auth.token;

import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.domain.UserAccount;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

/** Issues and validates HS256 access / refresh tokens. Secrets come from configuration, never source. */
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final Clock clock;

    public JwtService(String secret, Duration accessTtl, Duration refreshTtl, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.clock = clock;
    }

    public TokenPair issue(UserAccount user) {
        return new TokenPair(
                build(user, TYPE_ACCESS, accessTtl),
                build(user, TYPE_REFRESH, refreshTtl),
                accessTtl.toSeconds());
    }

    public TokenClaims parse(String token, String expectedType) {
        try {
            Claims c = Jwts.parser().verifyWith(key).clock(() -> Date.from(clock.instant()))
                    .build().parseSignedClaims(token).getPayload();
            if (!expectedType.equals(c.get("typ", String.class))) {
                throw new OenexaException(ErrorCode.UNAUTHORIZED, "Wrong token type");
            }
            return new TokenClaims(UUID.fromString(c.getSubject()),
                    UUID.fromString(c.get("tid", String.class)),
                    Role.valueOf(c.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            throw new OenexaException(ErrorCode.UNAUTHORIZED, "Invalid or expired token", e);
        }
    }

    private String build(UserAccount user, String type, Duration ttl) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(user.id().toString())
                .claim("tid", user.tenantId().toString())
                .claim("role", user.role().name())
                .claim("typ", type)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public record TokenClaims(UUID userId, UUID tenantId, Role role) {
    }
}
