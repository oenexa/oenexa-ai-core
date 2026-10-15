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
        assertThat(agent.type()).isEqualTo(AgentType.REPOSITORY);
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

    @Test
    void agentModuleBeanDefinitionsAndMetadata() {
        var module = new AgentModule();
        assertThat(module.id()).isEqualTo("agents");
        assertThat(module.displayName()).isEqualTo("OENEXA AI Agents");
        assertThat(module.description()).contains("15 specialised agents");
        assertThat(module.capabilities()).contains("coordinator", "workflows", "delegation", "agent-memory", "tool-registry");
        assertThat(module.status()).isEqualTo(ai.oenexa.common.module.PlatformModule.ModuleStatus.ACTIVE);

        var reasoner = module.agentReasoner();
        assertThat(reasoner).isInstanceOf(TemplateReasoner.class);

        org.springframework.beans.factory.ObjectProvider<Tool> provider = new org.springframework.beans.factory.ObjectProvider<>() {
            @Override
            public Tool getObject(Object... args) { return null; }
            @Override
            public Tool getIfAvailable() { return null; }
            @Override
            public Tool getIfUnique() { return null; }
            @Override
            public Tool getObject() { return null; }
            @Override
            public java.util.stream.Stream<Tool> orderedStream() {
                return java.util.stream.Stream.of(new Tool() {
                    public String name() { return "linter"; }
                    public Mono<String> call(String input) { return Mono.just("clean"); }
                });
            }
        };

        var toolRegistry = module.toolRegistry(provider);
        assertThat(toolRegistry.names()).contains("linter");

        var coordinator = module.coordinator(reasoner, toolRegistry);
        assertThat(coordinator).isNotNull();
    }

    @Test
    void domainContractsRecordsAndReflection() throws Exception {
        var catalogConstructor = AgentCatalog.class.getDeclaredConstructor();
        catalogConstructor.setAccessible(true);
        var catalogInstance = catalogConstructor.newInstance();
        assertThat(catalogInstance).isNotNull();

        var resultOf = Agent.Result.of("simple summary");
        assertThat(resultOf.summary()).isEqualTo("simple summary");
        assertThat(resultOf.artifacts()).isEmpty();
        assertThat(resultOf.delegation()).isEmpty();

        var delegation = new Agent.Delegation(AgentType.SECURITY, "audit secrets");
        assertThat(delegation.to()).isEqualTo(AgentType.SECURITY);
        assertThat(delegation.objective()).isEqualTo("audit secrets");

        var task = new Agent.Task(AgentType.REVIEW, "check PR");
        assertThat(task.agent()).isEqualTo(AgentType.REVIEW);
        assertThat(task.objective()).isEqualTo("check PR");

        var ctx = new AgentContext("primary goal");
        assertThat(ctx.objective()).isEqualTo("primary goal");
        ctx.log("step 1 complete");
        assertThat(ctx.transcript()).contains("step 1 complete");

        for (var wf : Workflow.values()) {
            assertThat(wf.steps()).isNotEmpty();
        }

        var run = new Coordinator(AgentCatalog.build(new TemplateReasoner(), new Tool.Registry()))
                .run(tenant, Workflow.PR_REVIEW, "test run accessors").block();
        assertThat(run.id()).isNotNull();
        assertThat(run.workflow()).isEqualTo(Workflow.PR_REVIEW);
        assertThat(run.status()).isEqualTo(Coordinator.Status.COMPLETED);
        assertThat(run.steps()).isNotEmpty();
        assertThat(run.report()).isNotEmpty();
        assertThat(run.error()).isNull();
        assertThat(run.startedAt()).isNotNull();
        assertThat(run.finishedAt()).isNotNull();

        var step = run.steps().get(0);
        assertThat(step.agent()).isNotNull();
        assertThat(step.objective()).isNotNull();
        assertThat(step.summary()).isNotNull();
        assertThat(step.at()).isNotNull();
        assertThat(step.depth()).isGreaterThanOrEqualTo(0);
    }
}
