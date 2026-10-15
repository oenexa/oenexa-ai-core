package ai.oenexa.auth.application;

import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.domain.UserAccount;
import ai.oenexa.auth.domain.UserAccountPort;
import ai.oenexa.auth.token.JwtService;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public class AuthService {

    private final UserAccountPort users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserAccountPort users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Self-service sign-up: creates a new tenant owned by the user. */
    public Mono<JwtService.TokenPair> register(String email, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 12) {
            return Mono.error(new OenexaException(ErrorCode.VALIDATION, "Password must be at least 12 characters"));
        }
        return users.findByEmail(email)
                .flatMap(existing -> Mono.<UserAccount>error(
                        new OenexaException(ErrorCode.CONFLICT, "Email already registered")))
                .switchIfEmpty(Mono.defer(() -> users.save(new UserAccount(
                        UUID.randomUUID(), UUID.randomUUID(), email.toLowerCase(),
                        encoder.encode(rawPassword), Role.OWNER, true))))
                .map(jwt::issue);
    }

    public Mono<JwtService.TokenPair> login(String email, String rawPassword) {
        // Same error for unknown user and wrong password: no account enumeration.
        Mono<UserAccount> invalid = Mono.error(new OenexaException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));
        return users.findByEmail(email)
                .filter(u -> u.enabled() && encoder.matches(rawPassword, u.passwordHash()))
                .switchIfEmpty(invalid)
                .map(jwt::issue);
    }

    public Mono<JwtService.TokenPair> refresh(String refreshToken) {
        return Mono.fromSupplier(() -> jwt.parse(refreshToken, JwtService.TYPE_REFRESH))
                .flatMap(c -> users.findById(c.userId()))
                .filter(UserAccount::enabled)
                .switchIfEmpty(Mono.error(new OenexaException(ErrorCode.UNAUTHORIZED, "Invalid refresh token")))
                .map(jwt::issue);
    }
}
