package com.oenexa.domain.model.common;

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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared blackboard context passed across coordinating AI agents.
 */
public class ExecutionContext {

    private final String objective;
    private final Map<String, String> shared = new ConcurrentHashMap<>();

    public ExecutionContext(String objective) {
        this.objective = Objects.requireNonNull(objective, "objective must not be null");
    }

    public String objective() {
        return objective;
    }

    public void put(String key, String value) {
        if (key != null && value != null) {
            shared.put(key, value);
        }
    }

    public String get(String key) {
        return shared.get(key);
    }

    public Map<String, String> shared() {
        return Map.copyOf(shared);
    }
}

