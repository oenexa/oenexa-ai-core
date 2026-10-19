package com.oenexa.application;

import com.oenexa.domain.EnterpriseBillingDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Subscription & Billing Service (Phase 11).
 * Manages Free, Pro, Business, and Enterprise plans, usage quotas, Stripe checkout,
 * invoice records, and API credits.
 */
@Service
public class EnterpriseBillingService {

    private final Map<String, UserSubscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, UsageQuota> usageByUserId = new ConcurrentHashMap<>();
    private final Map<String, List<InvoiceRecord>> invoicesByUserId = new ConcurrentHashMap<>();
    private final Map<String, ApiCreditBalance> creditsByUserId = new ConcurrentHashMap<>();

    public UserSubscription getSubscription(String userId, String tenantId) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        String safeTenant = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant";

        return subscriptions.computeIfAbsent(safeUser, u -> new UserSubscription(
                UUID.randomUUID(),
                safeUser,
                safeTenant,
                SubscriptionTier.PRO,
                BillingInterval.MONTHLY,
                SubscriptionStatus.ACTIVE,
                Instant.now(),
                Instant.now().plusSeconds(30L * 86400),
                false,
                "cus_" + UUID.randomUUID().toString().substring(0, 8),
                "sub_" + UUID.randomUUID().toString().substring(0, 8)
        ));
    }

    public UserSubscription changeSubscriptionTier(String userId, SubscriptionTier targetTier, BillingInterval interval) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        SubscriptionTier safeTier = (targetTier != null) ? targetTier : SubscriptionTier.PRO;
        BillingInterval safeInterval = (interval != null) ? interval : BillingInterval.MONTHLY;

        UserSubscription current = getSubscription(safeUser, null);
        UserSubscription updated = new UserSubscription(
                current.subscriptionId(),
                current.userId(),
                current.tenantId(),
                safeTier,
                safeInterval,
                SubscriptionStatus.ACTIVE,
                Instant.now(),
                Instant.now().plusSeconds(safeInterval == BillingInterval.ANNUAL ? 365L * 86400 : 30L * 86400),
                false,
                current.stripeCustomerId(),
                current.stripeSubscriptionId()
        );

        subscriptions.put(safeUser, updated);

        // Update quota limits based on new tier
        UsageQuota currentUsage = getUsageQuota(safeUser);
        usageByUserId.put(safeUser, new UsageQuota(
                currentUsage.tokensUsed(),
                safeTier.monthlyTokenQuota(),
                currentUsage.agentRunsUsed(),
                safeTier.monthlyAgentRunQuota(),
                currentUsage.storageMbUsed(),
                safeTier.storageMbQuota()
        ));

        // Generate invoice record for upgrade
        double amount = safeTier.monthlyPriceUsd() * (safeInterval == BillingInterval.ANNUAL ? 10 : 1);
        if (amount > 0) {
            String invId = "in_" + UUID.randomUUID().toString().substring(0, 10);
            invoicesByUserId.computeIfAbsent(safeUser, k -> new ArrayList<>()).add(0, new InvoiceRecord(
                    invId,
                    safeUser,
                    amount,
                    "USD",
                    "PAID",
                    Instant.now(),
                    "https://billing.stripe.com/p/invoices/" + invId,
                    "https://billing.stripe.com/p/invoices/" + invId + "/pdf"
            ));
        }

        return updated;
    }

    public StripeCheckoutSession createStripeCheckoutSession(
            String userId,
            SubscriptionTier tier,
            BillingInterval interval,
            String successUrl,
            String cancelUrl
    ) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        SubscriptionTier safeTier = (tier != null) ? tier : SubscriptionTier.PRO;
        BillingInterval safeInterval = (interval != null) ? interval : BillingInterval.MONTHLY;

        String sessionId = "cs_" + UUID.randomUUID().toString().replace("-", "");
        String checkoutUrl = String.format("https://checkout.stripe.com/c/pay/%s?client_reference_id=%s&tier=%s",
                sessionId, safeUser, safeTier.name());

        return new StripeCheckoutSession(sessionId, checkoutUrl, safeTier, safeInterval);
    }

    public UsageQuota getUsageQuota(String userId) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        UserSubscription sub = getSubscription(safeUser, null);

        return usageByUserId.computeIfAbsent(safeUser, u -> new UsageQuota(
                125_400L,
                sub.tier().monthlyTokenQuota(),
                12,
                sub.tier().monthlyAgentRunQuota(),
                240,
                sub.tier().storageMbQuota()
        ));
    }

    public UsageQuota trackUsage(String userId, long tokens, int agentRuns, int storageMb) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        UsageQuota current = getUsageQuota(safeUser);

        UsageQuota updated = new UsageQuota(
                current.tokensUsed() + Math.max(0, tokens),
                current.tokenLimit(),
                current.agentRunsUsed() + Math.max(0, agentRuns),
                current.agentRunsLimit(),
                current.storageMbUsed() + Math.max(0, storageMb),
                current.storageMbLimit()
        );

        usageByUserId.put(safeUser, updated);
        return updated;
    }

    public List<InvoiceRecord> listInvoices(String userId) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        return invoicesByUserId.computeIfAbsent(safeUser, u -> new ArrayList<>(List.of(
                new InvoiceRecord("in_101", safeUser, 29.0, "USD", "PAID", Instant.now().minusSeconds(86400 * 30), "https://billing.stripe.com/p/invoices/in_101", null),
                new InvoiceRecord("in_102", safeUser, 29.0, "USD", "PAID", Instant.now(), "https://billing.stripe.com/p/invoices/in_102", null)
        )));
    }

    public ApiCreditBalance getCreditBalance(String userId) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        return creditsByUserId.computeIfAbsent(safeUser, u -> new ApiCreditBalance(safeUser, 50.0, 100.0, Instant.now()));
    }

    public ApiCreditBalance addApiCredits(String userId, double amountUsd) {
        String safeUser = (userId != null && !userId.isBlank()) ? userId : "default-user";
        double safeAmount = Math.max(0.0, amountUsd);
        ApiCreditBalance current = getCreditBalance(safeUser);
        ApiCreditBalance updated = current.addCredits(safeAmount);
        creditsByUserId.put(safeUser, updated);
        return updated;
    }
}
