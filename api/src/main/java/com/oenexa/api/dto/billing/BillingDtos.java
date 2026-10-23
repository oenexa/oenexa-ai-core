package com.oenexa.api.dto.billing;

/**
 * Standard Data Transfer Objects for Enterprise Billing and Quota Management.
 */
public final class BillingDtos {

    private BillingDtos() {}

    public record SubscriptionUpgradeRequestDto(
            String userId,
            String tier,
            String interval
    ) {}

    public record SubscriptionStatusDto(
            String userId,
            String tier,
            String status,
            String interval,
            long tokenQuotaLimit,
            long tokenQuotaUsed
    ) {}

    public record StripeCheckoutRequestDto(
            String userId,
            String tier,
            String interval,
            String successUrl,
            String cancelUrl
    ) {}

    public record StripeCheckoutResponseDto(
            String sessionId,
            String checkoutUrl,
            String customerId,
            String status
    ) {}

    public record QuotaUsageDto(
            String userId,
            long tokenLimit,
            long tokenUsed,
            double remainingPercentage
    ) {}
}
