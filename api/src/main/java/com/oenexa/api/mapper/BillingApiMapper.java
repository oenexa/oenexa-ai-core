package com.oenexa.api.mapper;

import com.oenexa.api.dto.billing.BillingDtos.*;
import com.oenexa.domain.model.orchestration.EnterpriseBillingDomain.*;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Billing & Subscription DTOs.
 */
@Component
public class BillingApiMapper {

    public SubscriptionStatusDto toSubscriptionStatusDto(UserSubscription sub) {
        if (sub == null) {
            return null;
        }
        return new SubscriptionStatusDto(
                sub.userId(),
                sub.tier() != null ? sub.tier().name() : "FREE",
                sub.status() != null ? sub.status().name() : "ACTIVE",
                sub.interval() != null ? sub.interval().name() : "MONTHLY",
                sub.tier() != null ? sub.tier().monthlyTokenQuota() : 100_000,
                sub.tier() != null ? sub.tier().monthlyTokenQuota() / 10 : 0
        );
    }

    public StripeCheckoutResponseDto toStripeCheckoutResponseDto(StripeCheckoutSession session) {
        if (session == null) {
            return null;
        }
        return new StripeCheckoutResponseDto(
                session.sessionId(),
                session.checkoutUrl(),
                "cus_stripe_" + session.sessionId(),
                "OPEN"
        );
    }

    public QuotaUsageDto toQuotaUsageDto(UsageQuota quota) {
        if (quota == null) {
            return null;
        }
        return new QuotaUsageDto(
                "default-user",
                quota.tokenLimit(),
                quota.tokensUsed(),
                Math.max(0.0, 100.0 - quota.tokenUsagePercent())
        );
    }
}
