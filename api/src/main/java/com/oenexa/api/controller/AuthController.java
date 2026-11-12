package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.auth.AuthDtos.LoginRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.RegisterRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.TokenResponseDto;
import com.oenexa.api.dto.auth.AuthDtos.UserProfileDto;
import com.oenexa.api.mapper.AuthApiMapper;
import com.oenexa.application.service.auth.EnterpriseAuthService;
import com.oenexa.application.service.auth.TokenService;
import com.oenexa.application.security.AuthSecurityPolicy;
import com.oenexa.application.validation.AuthValidator;
import com.oenexa.domain.model.auth.AuthDomain.*;
import com.oenexa.domain.exception.OenexaExceptions.OenexaAuthenticationException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import com.oenexa.infrastructure.adapter.JpaUserStorageAdapter;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Enterprise Banking-Grade Authentication REST Controller.
 * Provides user registration, JWT login, token refresh, 2FA, OTP, OAuth,
 * session management, device tracking, and user profile inspection.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final EnterpriseAuthService enterpriseAuthService;
    private final AuthValidator authValidator;
    private final AuthSecurityPolicy authSecurityPolicy;
    private final AuthApiMapper authApiMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(TokenService tokenService, UserRepository userRepository) {
        this(
                new EnterpriseAuthService(
                        new JpaUserStorageAdapter(userRepository),
                        tokenService,
                        new BCryptPasswordEncoder()::encode,
                        (raw, stored) -> stored != null && new BCryptPasswordEncoder().matches(raw, stored)
                ),
                tokenService,
                userRepository,
                new AuthValidator(),
                new AuthSecurityPolicy(),
                new AuthApiMapper()
        );
    }

    public AuthController(EnterpriseAuthService enterpriseAuthService, TokenService tokenService, UserRepository userRepository) {
        this(enterpriseAuthService, tokenService, userRepository, new AuthValidator(), new AuthSecurityPolicy(), new AuthApiMapper());
    }

    @Autowired
    public AuthController(
            EnterpriseAuthService enterpriseAuthService,
            TokenService tokenService,
            UserRepository userRepository,
            AuthValidator authValidator,
            AuthSecurityPolicy authSecurityPolicy,
            AuthApiMapper authApiMapper
    ) {
        this.enterpriseAuthService = Objects.requireNonNull(enterpriseAuthService, "enterpriseAuthService must not be null");
        this.tokenService = Objects.requireNonNull(tokenService, "tokenService must not be null");
        this.userRepository = userRepository;
        this.authValidator = authValidator != null ? authValidator : new AuthValidator();
        this.authSecurityPolicy = authSecurityPolicy != null ? authSecurityPolicy : new AuthSecurityPolicy();
        this.authApiMapper = authApiMapper != null ? authApiMapper : new AuthApiMapper();
    }

    public AuthValidator getAuthValidator() {
        return authValidator;
    }

    public AuthSecurityPolicy getAuthSecurityPolicy() {
        return authSecurityPolicy;
    }

    public AuthApiMapper getAuthApiMapper() {
        return authApiMapper;
    }

    @PostMapping("/v2/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> loginV2(@RequestBody LoginRequestDto requestDto) {
        LoginRequest request = authApiMapper.toDomainLogin(requestDto);
        authValidator.validateLogin(request);
        TokenPair tokens = enterpriseAuthService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", authApiMapper.toTokenDto(tokens)));
    }

    @PostMapping("/v2/register")
    public ResponseEntity<ApiResponse<TokenResponseDto>> registerV2(@RequestBody RegisterRequestDto requestDto) {
        RegisterRequest request = authApiMapper.toDomainRegister(requestDto);
        authValidator.validateRegister(request);
        TokenPair tokens = enterpriseAuthService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registration successful", authApiMapper.toTokenDto(tokens)));
    }

    @GetMapping("/v2/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> meV2(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            throw new OenexaAuthenticationException("Missing or invalid Authorization header");
        }
        UserProfile profile = enterpriseAuthService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved", authApiMapper.toProfileDto(profile)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String identifier = request != null ? request.resolveIdentifier() : "";
        if (identifier.isBlank() || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username/email and password must not be empty"));
        }

        LoginRequest normalized = new LoginRequest(identifier, request.password(), request.device());
        try {
            authValidator.validateLogin(normalized);
            TokenPair tokens = enterpriseAuthService.login(normalized);
            return ResponseEntity.ok(tokens);
        } catch (OenexaValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("2FA_REQUIRED")) {
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(Map.of("status", "2FA_REQUIRED", "challenge", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, email, and password are required"));
        }
        String username = request.username();
        if ((username == null || username.isBlank()) && request.email() != null && request.email().contains("@")) {
            String derived = request.email().substring(0, request.email().indexOf('@')).trim();
            if (derived.length() >= 3) {
                username = derived;
            }
        }
        String fullName = request.fullName();
        if ((fullName == null || fullName.isBlank()) && (request.firstName() != null || request.lastName() != null)) {
            String f = request.firstName() != null ? request.firstName().trim() : "";
            String l = request.lastName() != null ? request.lastName().trim() : "";
            fullName = (f + " " + l).trim();
        }

        if (username == null || username.isBlank() || request.email() == null || request.email().isBlank() || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, email, and password are required"));
        }

        try {
            RegisterRequest normalized = new RegisterRequest(username, request.email(), request.password(), fullName);
            TokenPair tokens = enterpriseAuthService.register(normalized);
            return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }

        try {
            AuthClaims claims = tokenService.parse(request.refreshToken());
            TokenPair tokens = tokenService.issue(claims.userId(), claims.username(), claims.role());
            return ResponseEntity.ok(tokens);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        try {
            AuthClaims claims = tokenService.parse(token);
            try {
                Long uid = Long.parseLong(claims.userId());
                return ResponseEntity.ok(enterpriseAuthService.getUserProfile(uid));
            } catch (Exception ignored) {
                return ResponseEntity.ok(claims);
            }
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody UserProfileUpdateRequest request
    ) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        UserProfile updated = enterpriseAuthService.updateUserProfile(userId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<?> oauthLogin(
            @PathVariable("provider") String provider,
            @RequestBody OAuthLoginRequest request
    ) {
        OAuthLoginRequest req = new OAuthLoginRequest(
                provider.toUpperCase(),
                request.providerId(),
                request.email(),
                request.fullName(),
                request.avatarUrl(),
                request.accessToken()
        );
        TokenPair tokens = enterpriseAuthService.oauthLogin(req);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFactor(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        TwoFactorSetupResponse resp = enterpriseAuthService.setupTwoFactor(userId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactor(@RequestBody TwoFactorVerifyRequest request) {
        try {
            TokenPair tokens = enterpriseAuthService.loginWithTwoFactor(request);
            return ResponseEntity.ok(tokens);
        } catch (SecurityException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        String code = enterpriseAuthService.sendOtp(request);
        return ResponseEntity.ok(Map.of("status", "OTP_SENT", "email", request.email(), "code", code));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        boolean valid = enterpriseAuthService.verifyOtp(request);
        if (valid) {
            return ResponseEntity.ok(Map.of("status", "OTP_VERIFIED", "valid", true));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP code"));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            String token = enterpriseAuthService.requestPasswordReset(request);
            return ResponseEntity.ok(Map.of("status", "RESET_REQUESTED", "resetToken", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/password/reset-confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        try {
            enterpriseAuthService.confirmPasswordReset(request);
            return ResponseEntity.ok(Map.of("status", "PASSWORD_RESET_SUCCESS"));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> listSessions(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(enterpriseAuthService.listSessions(userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> revokeSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable("sessionId") String sessionId
    ) {
        enterpriseAuthService.revokeSession(sessionId);
        return ResponseEntity.ok(Map.of("status", "SESSION_REVOKED", "sessionId", sessionId));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(enterpriseAuthService.getAuditLogs(userId));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            AuthClaims claims = tokenService.parse(authHeader.substring(7));
            return Long.parseLong(claims.userId());
        } catch (Exception e) {
            return null;
        }
    }
}
