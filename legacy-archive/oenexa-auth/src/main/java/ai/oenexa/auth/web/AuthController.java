package ai.oenexa.auth.web;

import ai.oenexa.auth.application.AuthService;
import ai.oenexa.auth.token.JwtService.TokenPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    public record Credentials(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TokenPair> register(@Valid @RequestBody Credentials body) {
        return service.register(body.email(), body.password());
    }

    @PostMapping("/login")
    public Mono<TokenPair> login(@Valid @RequestBody Credentials body) {
        return service.login(body.email(), body.password());
    }

    @PostMapping("/refresh")
    public Mono<TokenPair> refresh(@Valid @RequestBody RefreshRequest body) {
        return service.refresh(body.refreshToken());
    }
}
