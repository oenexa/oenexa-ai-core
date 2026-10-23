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

/**
 * Standard Agent Types supported by OENEXA AI Engineer.
 * Why: Defines specialized AI agent roles with clear operational responsibilities.
 */
public enum AgentType {
    REPOSITORY,
    ARCHITECTURE,
    CODING,
    BUG_FIX,
    SECURITY,
    REVIEW,
    DOCUMENTATION,
    DEVOPS,
    KUBERNETES,
    DATABASE,
    TESTING,
    PRODUCT,
    RESEARCH,
    OBSERVABILITY,
    DEPLOYMENT
}

