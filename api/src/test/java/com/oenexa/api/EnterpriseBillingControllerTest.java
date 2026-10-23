package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.EnterpriseBillingService;
import com.oenexa.domain.EnterpriseBillingDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseBillingController Zero-Mock Unit Tests")
class EnterpriseBillingControllerTest {

    private EnterpriseBillingController controller;
    private EnterpriseBillingService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseBillingService();
        controller = new EnterpriseBillingController(service);
    }

    @Test
    @DisplayName("Should get and upgrade subscription via controller")
    void shouldGetAndUpgradeSubscription() {
        ResponseEntity<UserSubscription> subResp = controller.getSubscription("usr-001", "tenant-alpha");
        assertEquals(HttpStatus.OK, subResp.getStatusCode());
        assertNotNull(subResp.getBody());
        assertEquals(SubscriptionTier.PRO, subResp.getBody().tier());

        var upgradeReq = new EnterpriseBillingController.UpgradeSubscriptionRequest("usr-001", SubscriptionTier.ENTERPRISE, BillingInterval.ANNUAL);
        ResponseEntity<UserSubscription> upgradeResp = controller.upgradeSubscription(upgradeReq);
        assertEquals(HttpStatus.OK, upgradeResp.getStatusCode());
        assertNotNull(upgradeResp.getBody());
        assertEquals(SubscriptionTier.ENTERPRISE, upgradeResp.getBody().tier());
    }

    @Test
    @DisplayName("Should create Stripe checkout session and check quota via controller")
    void shouldCreateStripeCheckoutAndCheckQuota() {
        var stripeReq = new EnterpriseBillingController.StripeCheckoutRequest(
                "usr-001",
                SubscriptionTier.BUSINESS,
                BillingInterval.MONTHLY,
                "https://app.oenexa.ai/success",
                "https://app.oenexa.ai/cancel"
        );
        ResponseEntity<StripeCheckoutSession> stripeResp = controller.createStripeCheckout(stripeReq);
        assertEquals(HttpStatus.OK, stripeResp.getStatusCode());
        assertNotNull(stripeResp.getBody());
        assertTrue(stripeResp.getBody().checkoutUrl().contains("client_reference_id=usr-001"));

        ResponseEntity<UsageQuota> quotaResp = controller.getQuota("usr-001");
        assertEquals(HttpStatus.OK, quotaResp.getStatusCode());
        assertNotNull(quotaResp.getBody());
        assertTrue(quotaResp.getBody().tokenLimit() > 0);
    }

    @Test
    @DisplayName("Should list invoices and top up credits via controller")
    void shouldManageInvoicesAndTopUp() {
        ResponseEntity<List<InvoiceRecord>> invResp = controller.listInvoices("usr-001");
        assertEquals(HttpStatus.OK, invResp.getStatusCode());
        assertNotNull(invResp.getBody());
        assertFalse(invResp.getBody().isEmpty());

        ResponseEntity<ApiCreditBalance> credResp = controller.getCredits("usr-001");
        assertEquals(HttpStatus.OK, credResp.getStatusCode());
        assertNotNull(credResp.getBody());
        assertEquals(50.0, credResp.getBody().remainingCreditsUsd());

        var topUpReq = new EnterpriseBillingController.TopUpCreditsRequest("usr-001", 100.0);
        ResponseEntity<ApiCreditBalance> topUpResp = controller.topUpCredits(topUpReq);
        assertEquals(HttpStatus.OK, topUpResp.getStatusCode());
        assertNotNull(topUpResp.getBody());
        assertEquals(150.0, topUpResp.getBody().remainingCreditsUsd());
    }
}
