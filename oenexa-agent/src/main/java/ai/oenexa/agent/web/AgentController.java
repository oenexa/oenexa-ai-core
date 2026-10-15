package ai.oenexa.agent.web;

import ai.oenexa.agent.application.Coordinator;
import ai.oenexa.agent.application.Workflow;
import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    public record StartRun(@NotNull Workflow workflow, @NotBlank String objective) {
    }

    private final Coordinator coordinator;

    public AgentController(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    @GetMapping("/workflows")
    public List<Workflow> workflows() {
        return Arrays.asList(Workflow.values());
    }

    @PostMapping("/runs")
    public Mono<Coordinator.Run> start(Principal auth, @Valid @RequestBody StartRun body) {
        return coordinator.run(tenantOf(auth), body.workflow(), body.objective());
    }

    @GetMapping("/runs/{runId}")
    public Mono<Coordinator.Run> get(Principal auth, @PathVariable UUID runId) {
        return coordinator.get(tenantOf(auth), runId);
    }

    private static UUID tenantOf(Principal principal) {
        if (!(principal instanceof Authentication auth) || !(auth.getDetails() instanceof TokenClaims claims)) {
            throw new OenexaException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return claims.tenantId();
    }
}
