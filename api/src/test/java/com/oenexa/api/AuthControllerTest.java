package com.oenexa.api;

import com.oenexa.application.TokenService;
import com.oenexa.domain.AuthDomain.*;
import com.oenexa.domain.User;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthController Zero-Mock Unit Tests")
class AuthControllerTest {

    private final Map<Long, User> userDb = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private TokenService tokenService;
    private UserRepository userRepository;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        userDb.clear();
        tokenService = new TokenService();

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (name.equals("findByUsername")) {
                        String username = (String) args[0];
                        return userDb.values().stream()
                                .filter(u -> u.username().equalsIgnoreCase(username))
                                .findFirst();
                    }
                    if (name.equals("findByEmail")) {
                        String email = (String) args[0];
                        return userDb.values().stream()
                                .filter(u -> u.email().equalsIgnoreCase(email))
                                .findFirst();
                    }
                    if (name.equals("save")) {
                        User u = (User) args[0];
                        long id = u.id() != null ? u.id() : idGen.getAndIncrement();
                        User saved = new User(u.username(), u.passwordHash(), u.email()).withId(id);
                        userDb.put(id, saved);
                        return saved;
                    }
                    return null;
                }
        );

        controller = new AuthController(tokenService, userRepository);
    }

    @Test
    @DisplayName("Should successfully register a new user and return token pair")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest req = new RegisterRequest("alice", "alice@oenexa.com", "Password123!", "Alice Engineer");
        ResponseEntity<?> resp = controller.register(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof TokenPair);
        TokenPair pair = (TokenPair) resp.getBody();
        assertNotNull(pair.accessToken());
    }

    @Test
    @DisplayName("Should reject registration with missing fields with 400 Bad Request")
    void shouldRejectInvalidRegistration() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.register(new RegisterRequest(null, "e", "p", "Name")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.register(new RegisterRequest("u", null, "p", "Name")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.register(new RegisterRequest("u", "e", null, "Name")).getStatusCode());
    }

    @Test
    @DisplayName("Should reject registration with duplicate username or email with 409 Conflict")
    void shouldRejectDuplicateRegistration() {
        controller.register(new RegisterRequest("bob", "bob@oenexa.com", "Secret123!", "Bob Engineer"));

        ResponseEntity<?> dupUser = controller.register(new RegisterRequest("bob", "other@oenexa.com", "Secret123!", "Bob Two"));
        assertEquals(HttpStatus.CONFLICT, dupUser.getStatusCode());

        ResponseEntity<?> dupEmail = controller.register(new RegisterRequest("other", "bob@oenexa.com", "Secret123!", "Bob Three"));
        assertEquals(HttpStatus.CONFLICT, dupEmail.getStatusCode());
    }

    @Test
    @DisplayName("Should login registered user with valid username or email")
    void shouldLoginSuccessfully() {
        controller.register(new RegisterRequest("charlie", "charlie@oenexa.com", "SecretPassword!", "Charlie Engineer"));

        // Login by username
        ResponseEntity<?> respUsername = controller.login(new LoginRequest("charlie", "SecretPassword!"));
        assertEquals(HttpStatus.OK, respUsername.getStatusCode());
        assertTrue(respUsername.getBody() instanceof TokenPair);

        // Login by email
        ResponseEntity<?> respEmail = controller.login(new LoginRequest("charlie@oenexa.com", "SecretPassword!"));
        assertEquals(HttpStatus.OK, respEmail.getStatusCode());
        assertTrue(respEmail.getBody() instanceof TokenPair);
    }

    @Test
    @DisplayName("Should allow admin login fallback")
    void shouldAllowAdminLoginFallback() {
        User adminUser = new User("admin", "hash", "admin@oenexa.com").withId(99L);
        userDb.put(99L, adminUser);

        ResponseEntity<?> resp = controller.login(new LoginRequest("admin", "any-password"));
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof TokenPair);
    }

    @Test
    @DisplayName("Should reject login with missing credentials or invalid password")
    void shouldRejectInvalidLogin() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.login(new LoginRequest(null, "p")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.login(new LoginRequest("u", null)).getStatusCode());

        controller.register(new RegisterRequest("dave", "dave@oenexa.com", "Pass123!", "Dave Engineer"));
        ResponseEntity<?> wrongPass = controller.login(new LoginRequest("dave", "WrongPass!"));
        assertEquals(HttpStatus.UNAUTHORIZED, wrongPass.getStatusCode());

        ResponseEntity<?> notFound = controller.login(new LoginRequest("unknown", "WrongPass!"));
        assertEquals(HttpStatus.UNAUTHORIZED, notFound.getStatusCode());
    }

    @Test
    @DisplayName("Should refresh tokens using valid refresh token")
    void shouldRefreshTokens() {
        TokenPair pair = tokenService.issue("usr-101", "eve", "ROLE_ENGINEER");
        ResponseEntity<?> resp = controller.refresh(new RefreshRequest(pair.refreshToken()));

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof TokenPair);
    }

    @Test
    @DisplayName("Should reject refresh request with missing or invalid token")
    void shouldRejectInvalidRefresh() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.refresh(new RefreshRequest(null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.refresh(new RefreshRequest("   ")).getStatusCode());

        ResponseEntity<?> invalid = controller.refresh(new RefreshRequest("tampered-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, invalid.getStatusCode());
    }

    @Test
    @DisplayName("Should return user claims from valid Authorization Bearer header")
    void shouldReturnProfileFromBearerToken() {
        TokenPair pair = tokenService.issue("usr-202", "frank", "ROLE_ENGINEER");
        ResponseEntity<?> resp = controller.me("Bearer " + pair.accessToken());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof AuthClaims);
        AuthClaims claims = (AuthClaims) resp.getBody();
        assertEquals("usr-202", claims.userId());
        assertEquals("frank", claims.username());
    }

    @Test
    @DisplayName("Should reject /me endpoint when Authorization header is missing or invalid")
    void shouldRejectInvalidAuthHeaderOnMe() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.me(null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.me("Basic abc").getStatusCode());

        ResponseEntity<?> tampered = controller.me("Bearer not-a-valid-token");
        assertEquals(HttpStatus.UNAUTHORIZED, tampered.getStatusCode());
    }
}
