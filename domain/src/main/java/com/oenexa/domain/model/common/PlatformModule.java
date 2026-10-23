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

import java.util.List;

/**
 * Platform Module contract for discovery across the enterprise platform.
 */
public interface PlatformModule {

    String id();

    String displayName();

    String description();

    List<String> capabilities();

    default ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }

    default ModuleInfo info() {
        return new ModuleInfo(id(), displayName(), description(), capabilities(), status());
    }

    enum ModuleStatus { ACTIVE, PLANNED }

    record ModuleInfo(String id, String displayName, String description,
                      List<String> capabilities, ModuleStatus status) {}
}

