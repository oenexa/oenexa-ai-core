package ai.oenexa.rag.web;

import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import ai.oenexa.rag.application.RagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {

    public record IngestRequest(@NotBlank String source, @NotBlank @Size(max = 2_000_000) String text) {
    }

    public record QueryRequest(@NotBlank String query, Integer topK) {
    }

    private final RagService rag;

    public RagController(RagService rag) {
        this.rag = rag;
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, Object>> ingest(Principal auth, @Valid @RequestBody IngestRequest body) {
        UUID documentId = UUID.randomUUID();
        return rag.ingest(tenantOf(auth), documentId, body.source(), body.text())
                .map(n -> Map.of("documentId", documentId, "chunks", n));
    }

    @PostMapping("/query")
    public Mono<RagService.Context> query(Principal auth, @Valid @RequestBody QueryRequest body) {
        int k = body.topK() == null ? 5 : Math.min(Math.max(body.topK(), 1), 20);
        return rag.buildContext(tenantOf(auth), body.query(), k, 12_000);
    }

    private static UUID tenantOf(Principal principal) {
        if (principal instanceof Authentication auth && auth.getDetails() instanceof TokenClaims(var userId, var tenantId, var role)) {
            return tenantId;
        }
        throw new OenexaException(ErrorCode.UNAUTHORIZED, "Authentication required");
    }
}
