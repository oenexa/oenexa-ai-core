package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Enterprise Security Policy Layer for Knowledge Base & RAG Domain.
 * Guards document clearance boundaries and multi-tenant semantic knowledge partitions.
 */
@Component
public class KnowledgeSecurityPolicy {

    public void verifyDocumentClearance(String userClearance, String requiredClearance) {
        if (requiredClearance == null || "PUBLIC".equalsIgnoreCase(requiredClearance)) {
            return;
        }
        if (userClearance == null || userClearance.isBlank()) {
            throw new OenexaForbiddenException("Document clearance required for classified knowledge artifact", requiredClearance);
        }
        if ("CONFIDENTIAL".equalsIgnoreCase(requiredClearance) && "PUBLIC".equalsIgnoreCase(userClearance)) {
            throw new OenexaForbiddenException("User clearance 'PUBLIC' insufficient for 'CONFIDENTIAL' knowledge", "CONFIDENTIAL");
        }
        if ("RESTRICTED".equalsIgnoreCase(requiredClearance) && !"RESTRICTED".equalsIgnoreCase(userClearance)) {
            throw new OenexaForbiddenException("Top-secret knowledge requires RESTRICTED clearance", "RESTRICTED");
        }
    }
}

