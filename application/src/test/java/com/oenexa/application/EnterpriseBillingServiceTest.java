package com.oenexa.application;

import com.oenexa.domain.EnterpriseBillingDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseBillingService Zero-Mock Unit Tests")
class EnterpriseBillingServiceTest {

    private EnterpriseBillingService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseBillingService();
    }

    @Test
    @DisplayName("Should retrieve default PRO subscription and upgrade to BUSINESS")
    void shouldGetAndUpgradeSubscription() {
        UserSubscription sub = service.getSubscription("usr-prime", "tenant-alpha");
        assertNotNull(sub);
        assertEquals(SubscriptionTier.PRO, sub.tier());
        assertEquals(SubscriptionStatus.ACTIVE, sub.status());

        UserSubscription upgraded = service.changeSubscriptionTier("usr-prime", SubscriptionTier.BUSINESS, BillingInterval.ANNUAL);
        assertEquals(SubscriptionTier.BUSINESS, upgraded.tier());
        assertEquals(BillingInterval.ANNUAL, upgraded.interval());

        UsageQuota quota = service.getUsageQuota("usr-prime");
        assertEquals(SubscriptionTier.BUSINESS.monthlyTokenQuota(), quota.tokenLimit());
    }

    @Test
    @DisplayName("Should create Stripe Checkout session")
    void shouldCreateStripeCheckoutSession() {
        StripeCheckoutSession session = service.createStripeCheckoutSession(
                "usr-prime",
                SubscriptionTier.ENTERPRISE,
                BillingInterval.ANNUAL,
                "https://app.oenexa.ai/success",
                "https://app.oenexa.ai/cancel"
        );

        assertNotNull(session);
        assertEquals(SubscriptionTier.ENTERPRISE, session.tier());
        assertTrue(session.checkoutUrl().contains("client_reference_id=usr-prime"));
    }

    @Test
    @DisplayName("Should track usage, increment figures, and report quota state")
    void shouldTrackUsage() {
        UsageQuota initial = service.getUsageQuota("usr-prime");
        long initialTokens = initial.tokensUsed();

        UsageQuota updated = service.trackUsage("usr-prime", 5000L, 2, 10);
        assertEquals(initialTokens + 5000L, updated.tokensUsed());
        assertEquals(initial.agentRunsUsed() + 2, updated.agentRunsUsed());
        assertEquals(initial.storageMbUsed() + 10, updated.storageMbUsed());
    }

    @Test
    @DisplayName("Should list invoices and refill API credits")
    void shouldManageInvoicesAndCredits() {
        List<InvoiceRecord> invoices = service.listInvoices("usr-prime");
        assertFalse(invoices.isEmpty());
        assertEquals("USD", invoices.get(0).currency());

        ApiCreditBalance initialCredits = service.getCreditBalance("usr-prime");
        assertEquals(50.0, initialCredits.remainingCreditsUsd());

        ApiCreditBalance refilled = service.addApiCredits("usr-prime", 100.0);
        assertEquals(150.0, refilled.remainingCreditsUsd());
        assertEquals(200.0, refilled.totalPurchasedUsd());
    }
}
