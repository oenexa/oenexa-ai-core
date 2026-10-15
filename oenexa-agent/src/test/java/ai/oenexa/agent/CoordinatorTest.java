package ai.oenexa.agent;

import ai.oenexa.agent.application.AgentCatalog;
import ai.oenexa.agent.application.Coordinator;
import ai.oenexa.agent.application.RoleAgent;
import ai.oenexa.agent.application.TemplateReasoner;
import ai.oenexa.agent.application.Workflow;
import ai.oenexa.agent.domain.Agent;
import ai.oenexa.agent.domain.AgentContext;
import ai.oenexa.agent.domain.AgentMemory;
import ai.oenexa.agent.domain.AgentType;
import ai.oenexa.agent.domain.Tool;
import ai.oenexa.common.error.OenexaException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinatorTest {

    private final UUID tenant = UUID.randomUUID();

    @Test
    void runsRepositoryAnalysisPipelineInOrder() {
        var coordinator = new Coordinator(AgentCatalog.build(new TemplateReasoner(), new Tool.Registry()));

        StepVerifier.create(coordinator.run(tenant, Workflow.REPOSITORY_ANALYSIS, "analyse repo"))
                .assertNext(run -> {
                    assertThat(run.status()).isEqualTo(Coordinator.Status.COMPLETED);
                    assertThat(run.steps()).extracting(Coordinator.Step::agent)
                            .containsExactlyElementsOf(Workflow.REPOSITORY_ANALYSIS.steps());
                    assertThat(run.report()).contains("## REPOSITORY").contains("## DOCUMENTATION");
                })
                .verifyComplete();
    }

    @Test
    void securityFindingsDelegateToBugFixAgent() {
        var reasoner = (ai.oenexa.agent.domain.AgentReasoner) (prompt, input) ->
                Mono.just(prompt.contains("OWASP") ? "[FINDING] SQL injection in UserDao" : "patched");
        var coordinator = new Coordinator(AgentCatalog.build(reasoner, new Tool.Registry()));

        var run = coordinator.run(tenant, Workflow.PR_REVIEW, "review PR 42").block();

        assertThat(run.status()).isEqualTo(Coordinator.Status.COMPLETED);
        assertThat(run.steps()).filteredOn(s -> s.depth() == 1)
                .extracting(Coordinator.Step::agent).containsExactly(AgentType.BUG_FIX);
    }

    @Test
    void failedAgentMarksRunFailedButKeepsCompletedSteps() {
        Map<AgentType, Agent> agents = new EnumMap<>(AgentType.class);
        agents.put(AgentType.REPOSITORY, new RoleAgent(AgentType.REPOSITORY, "role", (p, i) -> Mono.just("ok"),
                new Tool.Registry(), List.of(), o -> Optional.empty()));
        agents.put(AgentType.REVIEW, new RoleAgent(AgentType.REVIEW, "role",
                (p, i) -> Mono.error(new IllegalStateException("llm down")),
                new Tool.Registry(), List.of(), o -> Optional.empty()));
        var coordinator = new Coordinator(agents);

        var run = coordinator.run(tenant, Workflow.PR_REVIEW, "x").block();

        assertThat(run.status()).isEqualTo(Coordinator.Status.FAILED);
        assertThat(run.error()).isEqualTo("llm down");
        assertThat(run.steps()).hasSize(1);
    }

    @Test
    void missingAgentFailsRun() {
        var run = new Coordinator(new EnumMap<>(AgentType.class)).run(tenant, Workflow.PR_REVIEW, "x").block();
        assertThat(run.status()).isEqualTo(Coordinator.Status.FAILED);
        assertThat(run.error()).contains("No agent registered");
    }

    @Test
    void runawayDelegationIsStopped() {
        Agent loop = new Agent() {
            public AgentType type() {
                return AgentType.CODING;
            }

            public Mono<Result> execute(Task t, AgentContext c) {
                return Mono.just(new Result("again", Map.of(),
                        Optional.of(new Delegation(AgentType.CODING, "again"))));
            }
        };
        var coordinator = new Coordinator(Map.of(AgentType.PRODUCT, loop, AgentType.CODING, loop));

        var run = coordinator.run(tenant, Workflow.FEATURE_DELIVERY, "x").block();

        assertThat(run.status()).isEqualTo(Coordinator.Status.FAILED);
        assertThat(run.error()).contains("Delegation depth");
    }

    @Test
    void runsAreTenantScoped() {
        var coordinator = new Coordinator(AgentCatalog.build(new TemplateReasoner(), new Tool.Registry()));
        var run = coordinator.run(tenant, Workflow.PR_REVIEW, "x").block();

        StepVerifier.create(coordinator.get(tenant, run.id())).expectNextCount(1).verifyComplete();
        StepVerifier.create(coordinator.get(UUID.randomUUID(), run.id()))
                .expectError(OenexaException.class).verify();
    }

    @Test
    void agentsUseToolsAndRememberOutputs() {
        var tools = new Tool.Registry().register(new Tool() {
            public String name() {
                return "git";
            }

            public Mono<String> call(String input) {
                return Mono.just("cloned");
            }
        });
        var seen = new java.util.concurrent.atomic.AtomicReference<String>();
        var agent = new RoleAgent(AgentType.REPOSITORY, "role", (p, i) -> {
            seen.set(i);
            return Mono.just("mapped");
        }, tools, List.of("git"), o -> Optional.empty());

        agent.execute(new Agent.Task(AgentType.REPOSITORY, "obj"), new AgentContext("obj")).block();

        assertThat(seen.get()).contains("[git] cloned");
        assertThat(agent.remembered()).containsExactly("mapped");
        assertThat(tools.names()).containsExactly("git");
    }

    @Test
    void unknownToolErrorsAndMemoryEvictsOldest() {
        StepVerifier.create(new Tool.Registry().call("nope", "x")).expectError(IllegalArgumentException.class).verify();

        var memory = new AgentMemory(2);
        memory.remember("a");
        memory.remember("b");
        memory.remember("c");
        assertThat(memory.recall()).containsExactly("b", "c");
        assertThat(AgentCatalog.role(AgentType.SECURITY)).contains("[FINDING]");
    }
}
