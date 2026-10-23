package com.oenexa.domain.model.orchestration;

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
import java.util.*;

/**
 * Enterprise Subscription & Billing Domain Model (Phase 11).
 * Encapsulates subscription tiers (Free, Pro, Business, Enterprise), Stripe integration,
 * quota tracking, invoices, and API credit management.
 */
public final class EnterpriseBillingDomain {

    private EnterpriseBillingDomain() {}

    public enum SubscriptionTier {
        FREE(0, 100_000L, 50, 1024),
        PRO(29, 2_000_000L, 1_000, 10_240),
        BUSINESS(99, 10_000_000L, 5_000, 51_200),
        ENTERPRISE(499, 100_000_000L, 50_000, 512_000);

        private final int monthlyPriceUsd;
        private final long monthlyTokenQuota;
        private final int monthlyAgentRunQuota;
        private final int storageMbQuota;

        SubscriptionTier(int monthlyPriceUsd, long monthlyTokenQuota, int monthlyAgentRunQuota, int storageMbQuota) {
            this.monthlyPriceUsd = monthlyPriceUsd;
            this.monthlyTokenQuota = monthlyTokenQuota;
            this.monthlyAgentRunQuota = monthlyAgentRunQuota;
            this.storageMbQuota = storageMbQuota;
        }

        public int monthlyPriceUsd() { return monthlyPriceUsd; }
        public long monthlyTokenQuota() { return monthlyTokenQuota; }
        public int monthlyAgentRunQuota() { return monthlyAgentRunQuota; }
        public int storageMbQuota() { return storageMbQuota; }
    }

    public enum BillingInterval {
        MONTHLY,
        ANNUAL
    }

    public enum SubscriptionStatus {
        ACTIVE,
        TRIALING,
        PAST_DUE,
        CANCELED,
        PAUSED
    }

    public record UsageQuota(
            long tokensUsed,
            long tokenLimit,
            int agentRunsUsed,
            int agentRunsLimit,
            int storageMbUsed,
            int storageMbLimit
    ) {
        public UsageQuota {
            if (tokensUsed < 0 || tokenLimit < 0 || agentRunsUsed < 0 || agentRunsLimit < 0) {
                throw new IllegalArgumentException("Usage quota figures must be non-negative");
            }
        }

        public double tokenUsagePercent() {
            return tokenLimit > 0 ? Math.min(100.0, (tokensUsed * 100.0) / tokenLimit) : 0.0;
        }

        public boolean isExceeded() {
            return tokensUsed > tokenLimit || agentRunsUsed > agentRunsLimit;
        }
    }

    public record UserSubscription(
            UUID subscriptionId,
            String userId,
            String tenantId,
            SubscriptionTier tier,
            BillingInterval interval,
            SubscriptionStatus status,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            boolean cancelAtPeriodEnd,
            String stripeCustomerId,
            String stripeSubscriptionId
    ) {
        public UserSubscription {
            Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            Objects.requireNonNull(tier, "tier must not be null");
            Objects.requireNonNull(interval, "interval must not be null");
            Objects.requireNonNull(status, "status must not be null");
            tenantId = (tenantId != null) ? tenantId : "default-tenant";
            currentPeriodStart = (currentPeriodStart != null) ? currentPeriodStart : Instant.now();
            currentPeriodEnd = (currentPeriodEnd != null) ? currentPeriodEnd : currentPeriodStart.plusSeconds(30L * 86400);
            stripeCustomerId = (stripeCustomerId != null) ? stripeCustomerId : "cus_default";
            stripeSubscriptionId = (stripeSubscriptionId != null) ? stripeSubscriptionId : "sub_default";
        }
    }

    public record InvoiceRecord(
            String invoiceId,
            String userId,
            double amountDueUsd,
            String currency,
            String status,
            Instant issuedAt,
            String hostedInvoiceUrl,
            String pdfUrl
    ) {
        public InvoiceRecord {
            Objects.requireNonNull(invoiceId, "invoiceId must not be null");
            Objects.requireNonNull(userId, "userId must not be null");
            currency = (currency != null) ? currency : "USD";
            status = (status != null) ? status : "PAID";
            issuedAt = (issuedAt != null) ? issuedAt : Instant.now();
            hostedInvoiceUrl = (hostedInvoiceUrl != null) ? hostedInvoiceUrl : "https://billing.stripe.com/p/invoices/" + invoiceId;
            pdfUrl = (pdfUrl != null) ? pdfUrl : hostedInvoiceUrl + "/pdf";
        }
    }

    public record ApiCreditBalance(
            String userId,
            double remainingCreditsUsd,
            double totalPurchasedUsd,
            Instant lastRefillAt
    ) {
        public ApiCreditBalance {
            Objects.requireNonNull(userId, "userId must not be null");
            if (remainingCreditsUsd < 0) {
                throw new IllegalArgumentException("remainingCreditsUsd cannot be negative");
            }
            lastRefillAt = (lastRefillAt != null) ? lastRefillAt : Instant.now();
        }

        public ApiCreditBalance addCredits(double amountUsd) {
            return new ApiCreditBalance(userId, remainingCreditsUsd + amountUsd, totalPurchasedUsd + amountUsd, Instant.now());
        }
    }

    public record StripeCheckoutSession(
            String sessionId,
            String checkoutUrl,
            SubscriptionTier tier,
            BillingInterval interval
    ) {
        public StripeCheckoutSession {
            Objects.requireNonNull(sessionId, "sessionId must not be null");
            Objects.requireNonNull(checkoutUrl, "checkoutUrl must not be null");
            Objects.requireNonNull(tier, "tier must not be null");
            Objects.requireNonNull(interval, "interval must not be null");
        }
    }
}

