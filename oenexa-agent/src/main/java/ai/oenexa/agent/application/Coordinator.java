package ai.oenexa.agent.application;

import ai.oenexa.agent.domain.Agent;
import ai.oenexa.agent.domain.AgentContext;
import ai.oenexa.agent.domain.AgentType;
import ai.oenexa.common.error.ErrorCode;
import ai.oenexa.common.error.OenexaException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Agent coordinator: routes tasks, shares context, executes workflows and follows delegations
 * (bounded depth) while recording a step timeline for the Agent Monitor UI.
 */
public class Coordinator {

    static final int MAX_DELEGATION_DEPTH = 3;

    public enum Status { COMPLETED, FAILED }

    public record Step(AgentType agent, String objective, String summary, Instant at, int depth) {
    }

    public record Run(UUID id, Workflow workflow, Status status, List<Step> steps, String report,
                      String error, Instant startedAt, Instant finishedAt) {
    }

    private final Map<AgentType, Agent> agents;
    private final Map<String, Run> runs = new ConcurrentHashMap<>();

    public Coordinator(Map<AgentType, Agent> agents) {
        this.agents = agents;
    }

    public Mono<Run> run(UUID tenantId, Workflow workflow, String objective) {
        UUID runId = UUID.randomUUID();
        Instant started = Instant.now();
        AgentContext ctx = new AgentContext(objective);
        List<Step> steps = new CopyOnWriteArrayList<>();

        return Flux.fromIterable(workflow.steps())
                .concatMap(type -> execute(type, objective, ctx, steps, 0))
                .then(Mono.fromSupplier(() -> finish(runId, workflow, Status.COMPLETED, steps, null, started)))
                .onErrorResume(e -> Mono.fromSupplier(
                        () -> finish(runId, workflow, Status.FAILED, steps, e.getMessage(), started)))
                .doOnNext(run -> runs.put(key(tenantId, runId), run));
    }

    public Mono<Run> get(UUID tenantId, UUID runId) {
        return Mono.justOrEmpty(runs.get(key(tenantId, runId)))
                .switchIfEmpty(Mono.error(new OenexaException(ErrorCode.NOT_FOUND, "Run not found")));
    }

    private Mono<Void> execute(AgentType type, String objective, AgentContext ctx, List<Step> steps, int depth) {
        if (depth > MAX_DELEGATION_DEPTH) {
            return Mono.error(new OenexaException(ErrorCode.CONFLICT, "Delegation depth exceeded"));
        }
        Agent agent = agents.get(type);
        if (agent == null) {
            return Mono.error(new OenexaException(ErrorCode.NOT_FOUND, "No agent registered for " + type));
        }
        return agent.execute(new Agent.Task(type, objective), ctx).flatMap(result -> {
            ctx.put(type.name(), result.summary());
            result.artifacts().forEach(ctx::put);
            steps.add(new Step(type, objective, result.summary(), Instant.now(), depth));
            return result.delegation()
                    .map(d -> execute(d.to(), d.objective(), ctx, steps, depth + 1))
                    .orElse(Mono.empty());
        });
    }

    private static Run finish(UUID id, Workflow wf, Status status, List<Step> steps, String error, Instant started) {
        StringBuilder report = new StringBuilder();
        steps.forEach(s -> report.append("## ").append(s.agent()).append('\n').append(s.summary()).append("\n\n"));
        return new Run(id, wf, status, List.copyOf(steps), report.toString().strip(), error, started, Instant.now());
    }

    private static String key(UUID tenantId, UUID runId) {
        return tenantId + ":" + runId;
    }
}
