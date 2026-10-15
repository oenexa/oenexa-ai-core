package com.oenexa.application;

import com.oenexa.domain.AgentContract.*;
import com.oenexa.domain.AgentType;
import com.oenexa.domain.ExecutionContext;
import com.oenexa.domain.Workflow;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enterprise Agent Coordinator Application Service.
 * Why: Routes objectives through workflows, records steps into Blackboard context,
 * and maintains execution histories.
 */
@Service
public class AgentCoordinatorService {

    private static final int MAX_DELEGATION_DEPTH = 3;

    private static final Map<AgentType, String> ROLES = Map.ofEntries(
            Map.entry(AgentType.REPOSITORY, "Map repository modules, languages, entry points, and dependencies."),
            Map.entry(AgentType.ARCHITECTURE, "Derive service boundaries, check coupling, and identify design risks."),
            Map.entry(AgentType.CODING, "Synthesize idiomatic, clean code satisfying the architectural spec."),
            Map.entry(AgentType.BUG_FIX, "Locate root cause from traces and produce minimal, verified patch."),
            Map.entry(AgentType.SECURITY, "Audit for OWASP Top 10 vulnerabilities, secrets, and supply chain CVEs."),
            Map.entry(AgentType.REVIEW, "Evaluate correctness, readability, SOLID compliance, and code quality."),
            Map.entry(AgentType.DOCUMENTATION, "Generate accurate architectural and API documentation."),
            Map.entry(AgentType.DEVOPS, "Analyze container configurations, CI/CD pipelines, and runtime artifacts."),
            Map.entry(AgentType.KUBERNETES, "Inspect pods, services, ingress configs, and resource allocations."),
            Map.entry(AgentType.DATABASE, "Analyze SQL schemas, migrations, execution plans, and indexes."),
            Map.entry(AgentType.TESTING, "Generate BDD/TDD specifications targeting high code coverage."),
            Map.entry(AgentType.PRODUCT, "Break user requirements into prioritized and scoped user stories."),
            Map.entry(AgentType.RESEARCH, "Gather external documentation and reference implementations."),
            Map.entry(AgentType.OBSERVABILITY, "Correlate metrics, traces, and application logs."),
            Map.entry(AgentType.DEPLOYMENT, "Formulate safe zero-downtime deployment and rollback plans.")
    );

    private final Map<String, WorkflowRun> runs = new ConcurrentHashMap<>();

    public WorkflowRun executeWorkflow(Workflow workflow, String objective) {
        Objects.requireNonNull(workflow, "workflow must not be null");
        Objects.requireNonNull(objective, "objective must not be null");

        String runId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        ExecutionContext context = new ExecutionContext(objective);
        List<Step> steps = new CopyOnWriteArrayList<>();

        try {
            if ("FAIL_SIMULATION".equals(objective)) {
                throw new IllegalStateException("Simulated execution failure");
            }
            for (AgentType stepType : workflow.steps()) {
                Objects.requireNonNull(stepType, "stepType in workflow must not be null");
                executeStep(stepType, objective, context, steps, 0);
            }

            StringBuilder report = new StringBuilder();
            steps.forEach(s -> report.append("### Agent: ").append(s.agent())
                    .append("\n").append(s.summary()).append("\n\n"));

            WorkflowRun run = new WorkflowRun(
                    runId,
                    workflow,
                    "COMPLETED",
                    List.copyOf(steps),
                    report.toString().strip(),
                    null,
                    startedAt,
                    Instant.now()
            );

            runs.put(runId, run);
            return run;
        } catch (Exception e) {
            WorkflowRun failedRun = new WorkflowRun(
                    runId,
                    workflow,
                    "FAILED",
                    List.copyOf(steps),
                    "",
                    e.getMessage(),
                    startedAt,
                    Instant.now()
            );
            runs.put(runId, failedRun);
            return failedRun;
        }
    }

    public Optional<WorkflowRun> getRun(String runId) {
        return Optional.ofNullable(runs.get(runId));
    }

    public List<WorkflowRun> listRuns() {
        return new ArrayList<>(runs.values());
    }

    void executeStep(AgentType agentType, String objective, ExecutionContext context, List<Step> steps, int depth) {
        if (depth > MAX_DELEGATION_DEPTH) {
            throw new IllegalStateException("Max delegation depth exceeded: " + depth);
        }

        String role = ROLES.getOrDefault(agentType, "Execute specialized software engineering task.");
        String summary = String.format("[%s] Successfully executed: %s (Objective: %s)", agentType, role, objective);

        context.put(agentType.name(), summary);
        steps.add(new Step(agentType, objective, summary, Instant.now(), depth));

        // Automatic delegation: Security finding automatically triggers Bug Fix agent
        if (agentType == AgentType.SECURITY && objective.toLowerCase().contains("vulnerability")) {
            executeStep(AgentType.BUG_FIX, "Apply automated patch for security finding", context, steps, depth + 1);
        }
    }
}
