package com.oenexa.domain.model.code;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

import java.util.List;

/**
 * Named, ordered agent execution workflows.
 * Why: Orchestrates multi-agent execution pipelines deterministically.
 */
public enum Workflow {

    REPOSITORY_ANALYSIS(List.of(
            AgentType.REPOSITORY,
            AgentType.ARCHITECTURE,
            AgentType.SECURITY,
            AgentType.BUG_FIX,
            AgentType.TESTING,
            AgentType.DOCUMENTATION
    )),
    PR_REVIEW(List.of(
            AgentType.REPOSITORY,
            AgentType.REVIEW,
            AgentType.SECURITY,
            AgentType.TESTING
    )),
    INCIDENT_RESPONSE(List.of(
            AgentType.OBSERVABILITY,
            AgentType.KUBERNETES,
            AgentType.BUG_FIX,
            AgentType.DEPLOYMENT
    )),
    FEATURE_DELIVERY(List.of(
            AgentType.PRODUCT,
            AgentType.ARCHITECTURE,
            AgentType.CODING,
            AgentType.TESTING,
            AgentType.REVIEW,
            AgentType.DEVOPS,
            AgentType.DEPLOYMENT
    ));

    private final List<AgentType> steps;

    Workflow(List<AgentType> steps) {
        this.steps = steps;
    }

    public List<AgentType> steps() {
        return steps;
    }
}

