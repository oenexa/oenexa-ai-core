package com.oenexa.domain.model.agent;

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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain contracts and value records for AI Agent executions.
 */
public interface AgentContract {

    AgentType type();

    Result execute(Task task, ExecutionContext context);

    record Task(AgentType agent, String objective) {}

    record Result(String summary, Map<String, String> artifacts, Optional<Delegation> delegation) {
        public static Result of(String summary) {
            return new Result(summary, Map.of(), Optional.empty());
        }
    }

    record Delegation(AgentType to, String objective) {}

    record Step(AgentType agent, String objective, String summary, Instant at, int depth) {}

    record WorkflowRun(String id, Workflow workflow, String status, List<Step> steps, String report,
                       String error, Instant startedAt, Instant finishedAt) {}
}

