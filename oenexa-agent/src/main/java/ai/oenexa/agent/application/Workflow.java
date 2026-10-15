package ai.oenexa.agent.application;

import ai.oenexa.agent.domain.AgentType;
import java.util.List;

/** Named, ordered pipelines the coordinator can execute. */
public enum Workflow {

    REPOSITORY_ANALYSIS(List.of(AgentType.REPOSITORY, AgentType.ARCHITECTURE, AgentType.SECURITY,
            AgentType.BUG_FIX, AgentType.TESTING, AgentType.DOCUMENTATION)),
    PR_REVIEW(List.of(AgentType.REPOSITORY, AgentType.REVIEW, AgentType.SECURITY, AgentType.TESTING)),
    INCIDENT_RESPONSE(List.of(AgentType.OBSERVABILITY, AgentType.KUBERNETES, AgentType.BUG_FIX,
            AgentType.DEPLOYMENT)),
    FEATURE_DELIVERY(List.of(AgentType.PRODUCT, AgentType.ARCHITECTURE, AgentType.CODING,
            AgentType.TESTING, AgentType.REVIEW, AgentType.DEVOPS, AgentType.DEPLOYMENT));

    private final List<AgentType> steps;

    Workflow(List<AgentType> steps) {
        this.steps = steps;
    }

    public List<AgentType> steps() {
        return steps;
    }
}
