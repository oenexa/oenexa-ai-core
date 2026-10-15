package ai.oenexa.gateway;

import ai.oenexa.auth.web.JwtAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http, JwtAuthenticationManager jwt) {
        AuthenticationWebFilter bearer = new AuthenticationWebFilter(jwt);
        bearer.setServerAuthenticationConverter(jwt);
        bearer.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                // Stateless bearer-token API: no cookies, so CSRF does not apply.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(bearer, SecurityWebFiltersOrder.AUTHENTICATION)
                .headers(h -> h.contentSecurityPolicy(c -> c.policyDirectives("default-src 'none'; frame-ancestors 'none'")))
                .authorizeExchange(ex -> ex
                        .pathMatchers("/api/v1/auth/**", "/api/v1/modules").permitAll()
                        .pathMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/agents/runs").hasAuthority("agent:run")
                        .pathMatchers("/api/v1/**").authenticated()
                        .anyExchange().denyAll())
                .build();
    }
}
