package ai.oenexa.auth;

import ai.oenexa.auth.application.AuthService;
import ai.oenexa.auth.domain.InMemoryUserAccountAdapter;
import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.token.JwtService;
import ai.oenexa.auth.web.JwtAuthenticationManager;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthFlowTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123456";
    private Clock clock;
    private JwtService jwt;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-10-14T09:00:00Z"), ZoneOffset.UTC);
        jwt = new JwtService(SECRET, Duration.ofMinutes(15), Duration.ofDays(7), clock);
        auth = new AuthService(new InMemoryUserAccountAdapter(), new BCryptPasswordEncoder(4), jwt);
    }

    @Test
    void registerThenLoginIssuesVerifiableTokens() {
        StepVerifier.create(auth.register("Dev@Example.com", "correct-horse-battery")
                        .then(auth.login("dev@example.com", "correct-horse-battery")))
                .assertNext(pair -> {
                    var claims = jwt.parse(pair.accessToken(), JwtService.TYPE_ACCESS);
                    assertThat(claims.role()).isEqualTo(Role.OWNER);
                    assertThat(pair.expiresInSeconds()).isEqualTo(900);
                })
                .verifyComplete();
    }

    @Test
    void wrongPasswordAndUnknownUserGiveSameError() {
        var registered = auth.register("a@example.com", "correct-horse-battery");
        StepVerifier.create(registered.then(auth.login("a@example.com", "nope-nope-nope-nope")))
                .expectErrorMessage("Invalid credentials").verify();
        StepVerifier.create(auth.login("ghost@example.com", "whatever-whatever"))
                .expectErrorMessage("Invalid credentials").verify();
    }

    @Test
    void duplicateRegistrationConflictsAndShortPasswordRejected() {
        StepVerifier.create(auth.register("a@example.com", "correct-horse-battery")
                        .then(auth.register("a@example.com", "correct-horse-battery")))
                .expectErrorSatisfies(e -> assertThat(((OenexaException) e).code()).isEqualTo(ErrorCode.CONFLICT))
                .verify();
        StepVerifier.create(auth.register("b@example.com", "short"))
                .expectErrorSatisfies(e -> assertThat(((OenexaException) e).code()).isEqualTo(ErrorCode.VALIDATION))
                .verify();
    }

    @Test
    void refreshRequiresRefreshTokenType() {
        var pair = auth.register("c@example.com", "correct-horse-battery").block();
        StepVerifier.create(auth.refresh(pair.refreshToken())).expectNextCount(1).verifyComplete();
        StepVerifier.create(auth.refresh(pair.accessToken())).expectError(OenexaException.class).verify();
    }

    @Test
    void expiredTokenIsRejected() {
        var pair = auth.register("d@example.com", "correct-horse-battery").block();
        var later = Clock.fixed(clock.instant().plus(Duration.ofHours(1)), ZoneOffset.UTC);
        var lateJwt = new JwtService(SECRET, Duration.ofMinutes(15), Duration.ofDays(7), later);
        assertThatThrownBy(() -> lateJwt.parse(pair.accessToken(), JwtService.TYPE_ACCESS))
                .isInstanceOf(OenexaException.class);
    }

    @Test
    void tamperedTokenAndWeakSecretAreRejected() {
        var pair = auth.register("e@example.com", "correct-horse-battery").block();
        assertThatThrownBy(() -> jwt.parse(pair.accessToken() + "x", JwtService.TYPE_ACCESS))
                .isInstanceOf(OenexaException.class);
        assertThatThrownBy(() -> new JwtService("short", Duration.ofMinutes(1), Duration.ofMinutes(1), clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bearerHeaderMapsToRoleAndPermissionAuthorities() {
        var pair = auth.register("f@example.com", "correct-horse-battery").block();
        var manager = new JwtAuthenticationManager(jwt);
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x")
                .header("Authorization", "Bearer " + pair.accessToken()).build());

        StepVerifier.create(manager.convert(exchange).flatMap(manager::authenticate))
                .assertNext(a -> {
                    var names = a.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).toList();
                    assertThat(names).contains("ROLE_OWNER", "agent:run");
                })
                .verifyComplete();

        var anonymous = MockServerWebExchange.from(MockServerHttpRequest.get("/x").build());
        StepVerifier.create(manager.convert(anonymous)).verifyComplete();
    }

    @Test
    void rolesExposeExpectedPermissions() {
        assertThat(Role.VIEWER.allows("project:read")).isTrue();
        assertThat(Role.VIEWER.allows("project:write")).isFalse();
        assertThat(Role.OWNER.allows("billing:manage")).isTrue();
    }
}
