package com.oenexa.api.filter;

import com.oenexa.application.service.auth.TokenService;
import com.oenexa.domain.model.auth.AuthDomain.AuthClaims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Resource-Server SSO Filter validating federated id.oenexa.com RS256 JWKS tokens
 * (via TokenService) and optionally trusted gateway context headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OenexaSsoAuthenticationFilter implements Filter {

    public static final String ATTR_USER_ID = "oenexa.userId";
    public static final String ATTR_USERNAME = "oenexa.username";
    public static final String ATTR_ROLE = "oenexa.role";

    private final TokenService tokenService;
    private final boolean enforceSso;
    private final boolean trustGatewayHeaders;

    @Autowired
    public OenexaSsoAuthenticationFilter(
            TokenService tokenService,
            @Value("${oenexa.security.sso.enforce:false}") boolean enforceSso,
            @Value("${oenexa.security.sso.trust-gateway-headers:false}") boolean trustGatewayHeaders
    ) {
        this.tokenService = Objects.requireNonNull(tokenService, "tokenService must not be null");
        this.enforceSso = enforceSso;
        this.trustGatewayHeaders = trustGatewayHeaders;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpReq) || !(response instanceof HttpServletResponse httpRes)) {
            chain.doFilter(request, response);
            return;
        }

        String path = httpReq.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod()) || isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpReq.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank()) {
            if (!authHeader.startsWith("Bearer ")) {
                writeUnauthorized(httpRes, "Authorization header must use Bearer scheme");
                return;
            }
            String token = authHeader.substring(7).trim();
            try {
                AuthClaims claims = tokenService.parse(token);
                bindUserContext(httpReq, claims.userId(), claims.username(), claims.role());
                try {
                    chain.doFilter(request, response);
                } finally {
                    MDC.remove("userId");
                }
                return;
            } catch (SecurityException ex) {
                writeUnauthorized(httpRes, ex.getMessage());
                return;
            }
        }

        if (trustGatewayHeaders) {
            String sub = firstNonBlank(httpReq.getHeader("X-Oenexa-Sub"), httpReq.getHeader("X-User-Id"));
            if (sub != null) {
                String roles = firstNonBlank(httpReq.getHeader("X-User-Roles"), "ROLE_USER");
                String primaryRole = roles.split(",")[0].trim();
                bindUserContext(httpReq, sub, firstNonBlank(httpReq.getHeader("X-User-Id"), sub), primaryRole);
                try {
                    chain.doFilter(request, response);
                } finally {
                    MDC.remove("userId");
                }
                return;
            }
        }

        if (enforceSso) {
            writeUnauthorized(httpRes, "Missing Bearer SSO token from id.oenexa.com");
            return;
        }

        chain.doFilter(request, response);
    }

    private static boolean isPublicPath(String path) {
        if (path == null) {
            return true;
        }
        return path.equals("/api/health")
                || path.equals("/api/status")
                || path.startsWith("/api/v1/auth/")
                || path.startsWith("/.well-known/")
                || path.startsWith("/oauth2/")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info");
    }

    private static void bindUserContext(HttpServletRequest req, String userId, String username, String role) {
        req.setAttribute(ATTR_USER_ID, userId);
        req.setAttribute(ATTR_USERNAME, username);
        req.setAttribute(ATTR_ROLE, role);
        if (userId != null && !userId.isBlank()) {
            MDC.put("userId", userId);
        }
    }

    private static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String escaped = message != null ? message.replace("\"", "'") : "Unauthorized";
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + escaped + "\"}");
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
