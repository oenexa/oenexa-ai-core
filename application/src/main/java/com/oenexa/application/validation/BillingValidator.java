package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Billing and Subscription Management.
 */
@Component
public class BillingValidator {

    public void validateUpgradeRequest(String userId, String tier, String interval) {
        List<String> violations = new ArrayList<>();
        if (userId == null || userId.trim().isBlank()) {
            violations.add("userId must not be blank");
        }
        if (tier == null || (!"FREE".equalsIgnoreCase(tier) && !"PRO".equalsIgnoreCase(tier) && !"ENTERPRISE".equalsIgnoreCase(tier) && !"SOVEREIGN".equalsIgnoreCase(tier))) {
            violations.add("tier must be one of FREE, PRO, ENTERPRISE, SOVEREIGN");
        }
        if (interval != null && (!"MONTHLY".equalsIgnoreCase(interval) && !"ANNUAL".equalsIgnoreCase(interval))) {
            violations.add("interval must be MONTHLY or ANNUAL");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Billing upgrade request validation failed", violations);
        }
    }

    public void validateStripeCheckout(String userId, String successUrl, String cancelUrl) {
        List<String> violations = new ArrayList<>();
        if (userId == null || userId.trim().isBlank()) {
            violations.add("userId must not be blank");
        }
        if (successUrl != null && !successUrl.startsWith("http://") && !successUrl.startsWith("https://")) {
            violations.add("successUrl must be a valid HTTP/HTTPS URL");
        }
        if (cancelUrl != null && !cancelUrl.startsWith("http://") && !cancelUrl.startsWith("https://")) {
            violations.add("cancelUrl must be a valid HTTP/HTTPS URL");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Stripe checkout request validation failed", violations);
        }
    }
}
