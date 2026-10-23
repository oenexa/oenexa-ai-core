package com.oenexa.application.security;

import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.OenexaExceptions.OenexaForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy Layer for Memory Hierarchy Domain.
 * Protects hardware memory mapping, air-gapped context tiers, and cross-workspace memory leak isolation.
 */
@Component
public class MemorySecurityPolicy {

    public void verifyTierAccess(MemoryLayerTier tier, boolean isAirGappedOnly) {
        if (tier == null) {
            return;
        }
        if (isAirGappedOnly && tier == MemoryLayerTier.L7_OBJECT_STORAGE_COLD) {
            throw new OenexaForbiddenException(
                    "Air-gapped operation prohibits offloading sensitive memory to external object storage (L7)",
                    "ROLE_SECURITY_OFFICER"
            );
        }
    }

    public void verifyWorkspaceMemoryIsolation(String callerWorkspaceId, String memoryWorkspaceId) {
        if (callerWorkspaceId == null || memoryWorkspaceId == null) {
            throw new OenexaForbiddenException("Memory isolation check failed: missing workspace context", "ROLE_USER");
        }
        if (!callerWorkspaceId.equalsIgnoreCase(memoryWorkspaceId) && !"global-system".equalsIgnoreCase(memoryWorkspaceId)) {
            throw new OenexaForbiddenException(
                    String.format("Cross-workspace memory breach attempt: caller '%s' cannot read memory owned by '%s'", callerWorkspaceId, memoryWorkspaceId),
                    "ROLE_WORKSPACE_ADMIN"
            );
        }
    }
}
