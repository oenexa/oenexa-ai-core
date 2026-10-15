package ai.oenexa.agent.application;

import ai.oenexa.agent.domain.Agent;
import ai.oenexa.agent.domain.AgentReasoner;
import ai.oenexa.agent.domain.AgentType;
import ai.oenexa.agent.domain.Tool;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Builds the full roster of 15 agents with their role prompts. */
public final class AgentCatalog {

    private AgentCatalog() {
    }

    private static final Map<AgentType, String> ROLES = new EnumMap<>(AgentType.class);

    static {
        ROLES.put(AgentType.REPOSITORY, "Map the repository: modules, languages, entry points, dependency graph.");
        ROLES.put(AgentType.ARCHITECTURE, "Derive architecture, service boundaries and coupling; flag design risks.");
        ROLES.put(AgentType.CODING, "Write minimal, idiomatic, tested code that satisfies the objective.");
        ROLES.put(AgentType.BUG_FIX, "Find root cause from logs and stack traces, propose the smallest safe patch.");
        ROLES.put(AgentType.SECURITY, "Audit for OWASP Top 10, secrets and vulnerable dependencies. Prefix issues with [FINDING].");
        ROLES.put(AgentType.REVIEW, "Review for correctness, readability, SOLID and performance; score risk and quality.");
        ROLES.put(AgentType.DOCUMENTATION, "Produce accurate technical documentation from the verified findings.");
        ROLES.put(AgentType.DEVOPS, "Analyse Dockerfiles, CI pipelines and deployment config; propose fixes.");
        ROLES.put(AgentType.KUBERNETES, "Diagnose CrashLoopBackOff, OOMKilled, ImagePullBackOff and resource issues.");
        ROLES.put(AgentType.DATABASE, "Analyse schemas, queries and execution plans; recommend indexes.");
        ROLES.put(AgentType.TESTING, "Design unit, integration and contract tests; target high coverage of changed code.");
        ROLES.put(AgentType.PRODUCT, "Turn requirements into scoped, prioritised user stories.");
        ROLES.put(AgentType.RESEARCH, "Gather and cross-check external sources; cite them.");
        ROLES.put(AgentType.OBSERVABILITY, "Correlate metrics, traces and logs; detect incidents and trends.");
        ROLES.put(AgentType.DEPLOYMENT, "Plan and verify a safe rollout with rollback criteria.");
    }

    public static Map<AgentType, Agent> build(AgentReasoner reasoner, Tool.Registry tools) {
        Map<AgentType, Agent> agents = new EnumMap<>(AgentType.class);
        for (AgentType type : AgentType.values()) {
            agents.put(type, new RoleAgent(type, ROLES.get(type), reasoner, tools, List.of(), delegationFor(type)));
        }
        return agents;
    }

    /** Security findings are handed to the Bug Fix agent automatically. */
    private static Function<String, Optional<Agent.Delegation>> delegationFor(AgentType type) {
        if (type == AgentType.SECURITY) {
            return out -> out.contains("[FINDING]")
                    ? Optional.of(new Agent.Delegation(AgentType.BUG_FIX, "Fix security findings: " + out))
                    : Optional.empty();
        }
        return out -> Optional.empty();
    }

    public static String role(AgentType type) {
        return ROLES.get(type);
    }
}
