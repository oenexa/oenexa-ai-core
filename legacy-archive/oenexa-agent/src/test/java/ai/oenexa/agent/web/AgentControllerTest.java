package ai.oenexa.agent.web;

import ai.oenexa.agent.application.AgentCatalog;
import ai.oenexa.agent.application.Coordinator;
import ai.oenexa.agent.application.TemplateReasoner;
import ai.oenexa.agent.application.Workflow;
import ai.oenexa.agent.domain.Tool;
import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.common.error.OenexaException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentControllerTest {

    private AgentController controller;
    private UsernamePasswordAuthenticationToken auth;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        var coordinator = new Coordinator(AgentCatalog.build(new TemplateReasoner(), new Tool.Registry()));
        controller = new AgentController(coordinator);
        tenantId = UUID.randomUUID();
        var claims = new TokenClaims(UUID.randomUUID(), tenantId, Role.DEVELOPER);
        auth = new UsernamePasswordAuthenticationToken("developer@oenexa.ai", "secret", List.of());
        auth.setDetails(claims);
    }

    @Test
    void workflowsEndpointReturnsAllConfiguredPipelines() {
        var workflows = controller.workflows();
        assertThat(workflows).containsExactly(Workflow.values());
    }

    @Test
    void startAndGetRunLifecycle() {
        var body = new AgentController.StartRun(Workflow.PR_REVIEW, "audit pull request 101");
        assertThat(body.workflow()).isEqualTo(Workflow.PR_REVIEW);
        assertThat(body.objective()).isEqualTo("audit pull request 101");

        var run = controller.start(auth, body).block();
        assertThat(run).isNotNull();
        assertThat(run.status()).isEqualTo(Coordinator.Status.COMPLETED);
        assertThat(run.report()).contains("REPOSITORY").contains("REVIEW");

        StepVerifier.create(controller.get(auth, run.id()))
                .assertNext(fetched -> {
                    assertThat(fetched.id()).isEqualTo(run.id());
                    assertThat(fetched.workflow()).isEqualTo(Workflow.PR_REVIEW);
                    assertThat(fetched.status()).isEqualTo(Coordinator.Status.COMPLETED);
                })
                .verifyComplete();
    }

    @Test
    void startRejectsMissingOrInvalidAuthentication() {
        var body = new AgentController.StartRun(Workflow.REPOSITORY_ANALYSIS, "check codebase");

        assertThatThrownBy(() -> controller.start(null, body))
                .isInstanceOf(OenexaException.class)
                .hasMessageContaining("Authentication required");

        Principal nonAuthPrincipal = () -> "anonymous";
        assertThatThrownBy(() -> controller.start(nonAuthPrincipal, body))
                .isInstanceOf(OenexaException.class)
                .hasMessageContaining("Authentication required");

        var badDetailsAuth = new UsernamePasswordAuthenticationToken("dev", "pass", List.of());
        badDetailsAuth.setDetails("invalid-details-string");
        assertThatThrownBy(() -> controller.start(badDetailsAuth, body))
                .isInstanceOf(OenexaException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    void getRejectsMissingAuthentication() {
        assertThatThrownBy(() -> controller.get(null, UUID.randomUUID()))
                .isInstanceOf(OenexaException.class)
                .hasMessageContaining("Authentication required");
    }
}
