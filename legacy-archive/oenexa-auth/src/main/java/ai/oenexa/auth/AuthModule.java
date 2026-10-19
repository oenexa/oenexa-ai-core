package ai.oenexa.auth;

import ai.oenexa.auth.application.AuthService;
import ai.oenexa.auth.domain.InMemoryUserAccountAdapter;
import ai.oenexa.auth.domain.UserAccountPort;
import ai.oenexa.auth.token.JwtService;
import ai.oenexa.auth.web.AuthController;
import ai.oenexa.auth.web.JwtAuthenticationManager;
import ai.oenexa.common.module.PlatformModule;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Import(AuthController.class)
public class AuthModule implements PlatformModule {

    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    JwtService jwtService(@Value("${oenexa.auth.jwt-secret}") String secret,
                          @Value("${oenexa.auth.access-ttl:PT15M}") Duration accessTtl,
                          @Value("${oenexa.auth.refresh-ttl:P7D}") Duration refreshTtl,
                          Clock clock) {
        return new JwtService(secret, accessTtl, refreshTtl, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @ConditionalOnMissingBean(UserAccountPort.class)
    UserAccountPort userAccountPort() {
        return new InMemoryUserAccountAdapter();
    }

    @Bean
    AuthService authService(UserAccountPort users, PasswordEncoder encoder, JwtService jwt) {
        return new AuthService(users, encoder, jwt);
    }

    @Bean
    JwtAuthenticationManager jwtAuthenticationManager(JwtService jwt) {
        return new JwtAuthenticationManager(jwt);
    }

    @Override
    public String id() {
        return "auth";
    }

    @Override
    public String displayName() {
        return "Enterprise Authentication";
    }

    @Override
    public String description() {
        return "JWT access/refresh tokens, RBAC, tenant-scoped identities.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("jwt", "refresh-tokens", "rbac", "tenant-claims");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
