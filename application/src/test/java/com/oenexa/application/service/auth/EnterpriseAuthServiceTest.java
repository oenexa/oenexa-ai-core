package com.oenexa.application.service.auth;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.auth.AuthDomain.*;
import com.oenexa.domain.model.auth.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseAuthService Zero-Mock Unit Tests")
class EnterpriseAuthServiceTest {

    private InMemoryUserStorage storage;
    private TokenService tokenService;
    private EnterpriseAuthService authService;

    static class InMemoryUserStorage implements UserStoragePort {
        private final Map<Long, User> usersById = new ConcurrentHashMap<>();
        private final AtomicLong idSeq = new AtomicLong(100);

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(usersById.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return usersById.values().stream()
                    .filter(u -> u.username().equalsIgnoreCase(username))
                    .findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return usersById.values().stream()
                    .filter(u -> u.email().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public User save(User user) {
            if (user.id() == null) {
                user.withId(idSeq.incrementAndGet());
            }
            usersById.put(user.id(), user);
            return user;
        }

        @Override
        public boolean existsByUsername(String username) {
            return findByUsername(username).isPresent();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email).isPresent();
        }
    }

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();
        tokenService = new TokenService();
        authService = new EnterpriseAuthService(storage, tokenService);
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest req = new RegisterRequest("alice", "alice@oenexa.ai", "Password123!", "Alice Engineer");
        TokenPair tokens = authService.register(req);

        assertNotNull(tokens);
        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());

        Optional<User> userOpt = storage.findByUsername("alice");
        assertTrue(userOpt.isPresent());
        assertEquals("Alice", userOpt.get().firstName());
        assertEquals("Engineer", userOpt.get().lastName());
        assertEquals("FREE", userOpt.get().subscriptionPlan());
    }

    @Test
    void shouldRejectInvalidRegistrationInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.register(new RegisterRequest("al", "alice@oenexa.ai", "Password123!", "Alice")));
        assertThrows(IllegalArgumentException.class, () ->
                authService.register(new RegisterRequest("alice", "invalid-email", "Password123!", "Alice")));
        assertThrows(IllegalArgumentException.class, () ->
                authService.register(new RegisterRequest("alice", "alice@oenexa.ai", "short", "Alice")));

        authService.register(new RegisterRequest("bob", "bob@oenexa.ai", "Password123!", "Bob"));
        assertThrows(IllegalStateException.class, () ->
                authService.register(new RegisterRequest("bob", "bob2@oenexa.ai", "Password123!", "Bob2")));
        assertThrows(IllegalStateException.class, () ->
                authService.register(new RegisterRequest("bob2", "bob@oenexa.ai", "Password123!", "Bob2")));
    }

    @Test
    void shouldAuthenticateValidUserAndIssueTokens() {
        authService.register(new RegisterRequest("charlie", "charlie@oenexa.ai", "SecurePass123!", "Charlie"));

        DeviceFingerprint device = new DeviceFingerprint("192.168.1.10", "Mozilla/5.0", "Laptop", "Chrome", "macOS", "US");
        TokenPair tokens = authService.login(new LoginRequest("charlie", "SecurePass123!", device));

        assertNotNull(tokens);
        AuthClaims claims = tokenService.parse(tokens.accessToken());
        assertEquals("charlie", claims.username());

        User user = storage.findByUsername("charlie").orElseThrow();
        List<UserSession> sessions = authService.listSessions(user.id());
        assertEquals(1, sessions.size());
        assertEquals("Chrome", sessions.get(0).device().browser());
    }

    @Test
    void shouldLockAccountAfterFiveConsecutiveFailedLogins() {
        authService.register(new RegisterRequest("dave", "dave@oenexa.ai", "SecurePass123!", "Dave"));

        for (int i = 0; i < 4; i++) {
            assertThrows(SecurityException.class, () -> authService.login(new LoginRequest("dave", "wrong-pass")));
        }

        User user = storage.findByUsername("dave").orElseThrow();
        assertFalse(user.isAccountLocked());
        assertEquals(4, user.failedLoginAttempts());

        // 5th failed attempt triggers lock
        assertThrows(SecurityException.class, () -> authService.login(new LoginRequest("dave", "wrong-pass")));
        assertTrue(user.isAccountLocked());

        // Subsequent attempt blocked due to lock
        SecurityException ex = assertThrows(SecurityException.class, () ->
                authService.login(new LoginRequest("dave", "SecurePass123!")));
        assertTrue(ex.getMessage().contains("locked"));
    }

    @Test
    void shouldHandleOAuthLoginForGoogleAndGithub() {
        OAuthLoginRequest req = new OAuthLoginRequest("GOOGLE", "g-12345", "oauth_user@gmail.com", "Google User", "https://avatar.url/g.png", "token-xyz");
        TokenPair tokens = authService.oauthLogin(req);

        assertNotNull(tokens);
        User user = storage.findByEmail("oauth_user@gmail.com").orElseThrow();
        assertEquals("Google", user.firstName());
        assertEquals("User", user.lastName());
        assertEquals("https://avatar.url/g.png", user.profileImageUrl());

        // Repeat login with same email should reuse existing user
        TokenPair tokens2 = authService.oauthLogin(req);
        assertNotNull(tokens2);
    }

    @Test
    void shouldSupportTwoFactorSetupAndVerification() {
        authService.register(new RegisterRequest("emma", "emma@oenexa.ai", "Password123!", "Emma"));
        User user = storage.findByUsername("emma").orElseThrow();

        TwoFactorSetupResponse setup = authService.setupTwoFactor(user.id());
        assertNotNull(setup.secret());
        assertTrue(setup.otpauthUrl().contains("emma@oenexa.ai"));
        assertEquals(8, setup.backupCodes().size());

        long window = System.currentTimeMillis() / 1000L / 30L;
        String validCode = EnterpriseAuthService.generateTotpCode(setup.secret(), window);

        TokenPair tokens = authService.verifyAndEnableTwoFactor(user.id(), validCode, setup.secret());
        assertNotNull(tokens);
        assertTrue(user.isTwoFactorEnabled());

        // Login should now require 2FA
        SecurityException challenge = assertThrows(SecurityException.class, () ->
                authService.login(new LoginRequest("emma", "Password123!")));
        assertTrue(challenge.getMessage().startsWith("2FA_REQUIRED"));

        // Complete 2FA login
        TokenPair finalTokens = authService.loginWithTwoFactor(new TwoFactorVerifyRequest("emma", validCode, setup.secret()));
        assertNotNull(finalTokens);
    }

    @Test
    void shouldSupportOtpGenerationAndVerification() {
        OtpRequest req = new OtpRequest("user@oenexa.ai", "LOGIN_CONFIRM");
        String otp = authService.sendOtp(req);

        assertNotNull(otp);
        assertEquals(6, otp.length());

        assertTrue(authService.verifyOtp(new OtpVerifyRequest("user@oenexa.ai", otp, "LOGIN_CONFIRM")));
        // Re-verifying used OTP should fail
        assertFalse(authService.verifyOtp(new OtpVerifyRequest("user@oenexa.ai", otp, "LOGIN_CONFIRM")));
    }

    @Test
    void shouldSupportPasswordResetWorkflow() {
        authService.register(new RegisterRequest("frank", "frank@oenexa.ai", "OldPass123!", "Frank"));

        String resetToken = authService.requestPasswordReset(new PasswordResetRequest("frank@oenexa.ai"));
        assertNotNull(resetToken);
        assertTrue(resetToken.startsWith("reset-"));

        authService.confirmPasswordReset(new PasswordResetConfirmRequest("frank@oenexa.ai", resetToken, "NewPass987!"));

        // Login with old password fails
        assertThrows(SecurityException.class, () -> authService.login(new LoginRequest("frank", "OldPass123!")));

        // Login with new password succeeds
        TokenPair tokens = authService.login(new LoginRequest("frank", "NewPass987!"));
        assertNotNull(tokens);
    }

    @Test
    void shouldInspectAndUpdateUserProfile() {
        authService.register(new RegisterRequest("grace", "grace@oenexa.ai", "Password123!", "Grace Hopper"));
        User user = storage.findByUsername("grace").orElseThrow();

        UserProfile profile = authService.getUserProfile(user.id());
        assertEquals("grace", profile.username());
        assertEquals("grace@oenexa.ai", profile.email());

        UserProfileUpdateRequest updateReq = new UserProfileUpdateRequest(
                "Grace", "Hopper", "United States", "Computer Scientist & Pioneer",
                "US Navy", "Chief Engineer", "en", "America/New_York", "https://avatar/grace.png"
        );
        UserProfile updated = authService.updateUserProfile(user.id(), updateReq);

        assertEquals("Grace", updated.firstName());
        assertEquals("Hopper", updated.lastName());
        assertEquals("United States", updated.country());
        assertEquals("America/New_York", updated.timezone());
        assertEquals("https://avatar/grace.png", updated.profileImageUrl());
    }

    @Test
    void shouldTrackSessionsAndAuditLogs() {
        authService.register(new RegisterRequest("heidi", "heidi@oenexa.ai", "Password123!", "Heidi"));
        User user = storage.findByUsername("heidi").orElseThrow();

        authService.login(new LoginRequest("heidi", "Password123!"));
        authService.login(new LoginRequest("heidi", "Password123!"));

        List<UserSession> sessions = authService.listSessions(user.id());
        assertEquals(2, sessions.size());

        authService.revokeSession(sessions.get(0).sessionId());
        assertEquals(1, authService.listSessions(user.id()).size());

        List<AuditLogEntry> auditLogs = authService.getAuditLogs(user.id());
        assertTrue(auditLogs.size() >= 3);
        assertTrue(auditLogs.stream().anyMatch(l -> "USER_REGISTERED".equals(l.action())));
        assertTrue(auditLogs.stream().anyMatch(l -> "LOGIN_SUCCESS".equals(l.action())));
    }
}


