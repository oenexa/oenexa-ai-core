package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.service.auth.TokenService;
import com.oenexa.domain.model.auth.AuthDomain.*;
import com.oenexa.domain.model.auth.User;
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
                    if (name.equals("findById")) {
                        Long id = (Long) args[0];
                        return Optional.ofNullable(userDb.get(id));
                    }
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
                    if (name.equals("existsByUsername")) {
                        String username = (String) args[0];
                        return userDb.values().stream().anyMatch(u -> u.username().equalsIgnoreCase(username));
                    }
                    if (name.equals("existsByEmail")) {
                        String email = (String) args[0];
                        return userDb.values().stream().anyMatch(u -> u.email().equalsIgnoreCase(email));
                    }
                    if (name.equals("save")) {
                        User u = (User) args[0];
                        long id = u.id() != null ? u.id() : idGen.getAndIncrement();
                        u.withId(id);
                        userDb.put(id, u);
                        return u;
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

    @Test
    @DisplayName("Should login via OAuth provider and return tokens")
    void shouldLoginViaOAuth() {
        OAuthLoginRequest req = new OAuthLoginRequest("GOOGLE", "g-1", "googleuser@gmail.com", "Google Dev", "https://avatar/g.png", "token-xyz");
        ResponseEntity<?> resp = controller.oauthLogin("GOOGLE", req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof TokenPair);
    }

    @Test
    @DisplayName("Should send and verify OTP codes successfully")
    void shouldSendAndVerifyOtp() {
        ResponseEntity<?> sendResp = controller.sendOtp(new OtpRequest("otp@test.com", "LOGIN"));
        assertEquals(HttpStatus.OK, sendResp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) sendResp.getBody();
        assertNotNull(body);
        String code = (String) body.get("code");

        ResponseEntity<?> verifyResp = controller.verifyOtp(new OtpVerifyRequest("otp@test.com", code, "LOGIN"));
        assertEquals(HttpStatus.OK, verifyResp.getStatusCode());

        ResponseEntity<?> failResp = controller.verifyOtp(new OtpVerifyRequest("otp@test.com", "000000", "LOGIN"));
        assertEquals(HttpStatus.BAD_REQUEST, failResp.getStatusCode());
    }

    @Test
    @DisplayName("Should execute complete password reset flow")
    void shouldExecutePasswordResetFlow() {
        controller.register(new RegisterRequest("resetuser", "reset@test.com", "Pass12345!", "Reset User"));

        ResponseEntity<?> reqResp = controller.requestPasswordReset(new PasswordResetRequest("reset@test.com"));
        assertEquals(HttpStatus.OK, reqResp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) reqResp.getBody();
        String token = (String) body.get("resetToken");

        ResponseEntity<?> confResp = controller.confirmPasswordReset(new PasswordResetConfirmRequest("reset@test.com", token, "BrandNewPass999!"));
        assertEquals(HttpStatus.OK, confResp.getStatusCode());

        // Login with new pass
        ResponseEntity<?> loginResp = controller.login(new LoginRequest("resetuser", "BrandNewPass999!"));
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
    }

    @Test
    @DisplayName("Should update user profile and list sessions")
    void shouldUpdateProfileAndListSessions() {
        ResponseEntity<?> regResp = controller.register(new RegisterRequest("profileuser", "prof@test.com", "Pass12345!", "Prof User"));
        TokenPair pair = (TokenPair) regResp.getBody();
        String authHeader = "Bearer " + pair.accessToken();

        UserProfileUpdateRequest updateReq = new UserProfileUpdateRequest(
                "Alan", "Turing", "UK", "Father of Computer Science", "National Lab", "Fellow", "en", "Europe/London", "https://avatar/turing.png"
        );
        ResponseEntity<?> updateResp = controller.updateProfile(authHeader, updateReq);
        assertEquals(HttpStatus.OK, updateResp.getStatusCode());

        ResponseEntity<?> sessionsResp = controller.listSessions(authHeader);
        assertEquals(HttpStatus.OK, sessionsResp.getStatusCode());

        ResponseEntity<?> auditResp = controller.auditLogs(authHeader);
        assertEquals(HttpStatus.OK, auditResp.getStatusCode());

        ResponseEntity<?> revokeResp = controller.revokeSession(authHeader, "session-123");
        assertEquals(HttpStatus.OK, revokeResp.getStatusCode());
    }

    @Test
    @DisplayName("Should setup and verify two-factor authentication")
    void shouldSetupAndVerifyTwoFactor() {
        ResponseEntity<?> regResp = controller.register(new RegisterRequest("twofa_user", "twofa@test.com", "Pass12345!", "2FA User"));
        TokenPair pair = (TokenPair) regResp.getBody();
        String authHeader = "Bearer " + pair.accessToken();

        ResponseEntity<?> setupResp = controller.setupTwoFactor(authHeader);
        assertEquals(HttpStatus.OK, setupResp.getStatusCode());
        TwoFactorSetupResponse setupData = (TwoFactorSetupResponse) setupResp.getBody();
        assertNotNull(setupData.secret());
        assertNotNull(setupData.otpauthUrl());

        // Unauthorized setup without header
        ResponseEntity<?> unauthSetup = controller.setupTwoFactor(null);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthSetup.getStatusCode());

        // Verify invalid code
        ResponseEntity<?> verifyResp = controller.verifyTwoFactor(new TwoFactorVerifyRequest("twofa_user", "999999", "secret123"));
        assertEquals(HttpStatus.UNAUTHORIZED, verifyResp.getStatusCode());

        // Unauthorized profile, sessions, and audit without valid header
        assertEquals(HttpStatus.UNAUTHORIZED, controller.updateProfile(null, new UserProfileUpdateRequest(null, null, null, null, null, null, null, null, null)).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.listSessions(null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.auditLogs(null).getStatusCode());
    }
}



