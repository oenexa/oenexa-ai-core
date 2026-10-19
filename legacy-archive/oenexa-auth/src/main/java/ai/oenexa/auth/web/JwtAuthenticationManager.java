package ai.oenexa.auth.web;

import ai.oenexa.auth.token.JwtService;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Bearer-token authentication: extracts the header and maps JWT claims to authorities. */
public class JwtAuthenticationManager implements ReactiveAuthenticationManager, ServerAuthenticationConverter {

    private static final String PREFIX = "Bearer ";
    private final JwtService jwt;

    public JwtAuthenticationManager(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX)) {
            return Mono.empty();
        }
        return Mono.just(new UsernamePasswordAuthenticationToken(null, header.substring(PREFIX.length())));
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.fromSupplier(() -> {
            try {
                var claims = jwt.parse((String) authentication.getCredentials(), JwtService.TYPE_ACCESS);
                Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
                claims.role().permissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
                var auth = new UsernamePasswordAuthenticationToken(claims.userId().toString(), null,
                        List.copyOf(authorities));
                auth.setDetails(claims);
                return auth;
            } catch (Exception ex) {
                throw new org.springframework.security.authentication.BadCredentialsException("Invalid token", ex);
            }
        });
    }
}
