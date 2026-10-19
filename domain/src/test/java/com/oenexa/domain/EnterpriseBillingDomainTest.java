package com.oenexa.domain;

import com.oenexa.domain.EnterpriseBillingDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseBillingDomain Invariant Tests (Zero-Mock)")
class EnterpriseBillingDomainTest {

    @Test
    @DisplayName("Should verify subscription tier quotas and monthly pricing")
    void shouldVerifyTierQuotas() {
        assertEquals(0, SubscriptionTier.FREE.monthlyPriceUsd());
        assertEquals(100_000L, SubscriptionTier.FREE.monthlyTokenQuota());

        assertEquals(29, SubscriptionTier.PRO.monthlyPriceUsd());
        assertEquals(2_000_000L, SubscriptionTier.PRO.monthlyTokenQuota());

        assertEquals(99, SubscriptionTier.BUSINESS.monthlyPriceUsd());
        assertEquals(10_000_000L, SubscriptionTier.BUSINESS.monthlyTokenQuota());

        assertEquals(499, SubscriptionTier.ENTERPRISE.monthlyPriceUsd());
        assertEquals(100_000_000L, SubscriptionTier.ENTERPRISE.monthlyTokenQuota());
    }

    @Test
    @DisplayName("Should calculate token usage percentage and detect quota exhaustion")
    void shouldCalculateUsagePercentage() {
        UsageQuota withinLimit = new UsageQuota(50_000L, 100_000L, 10, 50, 100, 1024);
        assertEquals(50.0, withinLimit.tokenUsagePercent());
        assertFalse(withinLimit.isExceeded());

        UsageQuota exceeded = new UsageQuota(120_000L, 100_000L, 60, 50, 100, 1024);
        assertEquals(100.0, exceeded.tokenUsagePercent());
        assertTrue(exceeded.isExceeded());
    }

    @Test
    @DisplayName("Should create UserSubscription and StripeCheckoutSession")
    void shouldCreateSubscriptionAndStripeSession() {
        UUID subId = UUID.randomUUID();
        UserSubscription sub = new UserSubscription(
                subId,
                "user-101",
                "tenant-alpha",
                SubscriptionTier.PRO,
                BillingInterval.MONTHLY,
                SubscriptionStatus.ACTIVE,
                Instant.now(),
                Instant.now().plusSeconds(30L * 86400),
                false,
                "cus_12345",
                "sub_98765"
        );

        assertNotNull(sub);
        assertEquals("user-101", sub.userId());
        assertEquals(SubscriptionTier.PRO, sub.tier());
        assertEquals(SubscriptionStatus.ACTIVE, sub.status());

        StripeCheckoutSession session = new StripeCheckoutSession(
                "cs_test_abc123",
                "https://checkout.stripe.com/c/pay/cs_test_abc123",
                SubscriptionTier.PRO,
                BillingInterval.MONTHLY
        );
        assertEquals("cs_test_abc123", session.sessionId());
        assertTrue(session.checkoutUrl().contains("checkout.stripe.com"));
    }

    @Test
    @DisplayName("Should manage ApiCreditBalance with top-up additions")
    void shouldManageCredits() {
        ApiCreditBalance credits = new ApiCreditBalance("user-101", 25.0, 50.0, Instant.now());
        assertEquals(25.0, credits.remainingCreditsUsd());

        ApiCreditBalance toppedUp = credits.addCredits(50.0);
        assertEquals(75.0, toppedUp.remainingCreditsUsd());
        assertEquals(100.0, toppedUp.totalPurchasedUsd());
    }
}
