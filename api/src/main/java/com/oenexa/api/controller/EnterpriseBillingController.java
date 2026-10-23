package com.oenexa.api.controller;

import com.oenexa.application.EnterpriseBillingService;
import com.oenexa.domain.EnterpriseBillingDomain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Enterprise Subscription & Billing REST Controller (Phase 11).
 * Exposes Subscription management, Stripe checkout, Quota tracking, Invoices, and API Credits.
 */
@RestController
@RequestMapping("/api/v1/billing")
@CrossOrigin(origins = "*")
public class EnterpriseBillingController {

    private final EnterpriseBillingService billingService;

    public EnterpriseBillingController(EnterpriseBillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/subscription")
    public ResponseEntity<UserSubscription> getSubscription(
            @RequestParam(value = "userId", required = false, defaultValue = "default-user") String userId,
            @RequestParam(value = "tenantId", required = false, defaultValue = "default-tenant") String tenantId
    ) {
        return ResponseEntity.ok(billingService.getSubscription(userId, tenantId));
    }

    @PostMapping("/subscription/upgrade")
    public ResponseEntity<UserSubscription> upgradeSubscription(@RequestBody UpgradeSubscriptionRequest req) {
        String userId = req.userId() != null ? req.userId() : "default-user";
        SubscriptionTier tier = req.tier() != null ? req.tier() : SubscriptionTier.PRO;
        BillingInterval interval = req.interval() != null ? req.interval() : BillingInterval.MONTHLY;
        return ResponseEntity.ok(billingService.changeSubscriptionTier(userId, tier, interval));
    }

    @PostMapping("/checkout/stripe")
    public ResponseEntity<StripeCheckoutSession> createStripeCheckout(@RequestBody StripeCheckoutRequest req) {
        String userId = req.userId() != null ? req.userId() : "default-user";
        SubscriptionTier tier = req.tier() != null ? req.tier() : SubscriptionTier.PRO;
        BillingInterval interval = req.interval() != null ? req.interval() : BillingInterval.MONTHLY;
        return ResponseEntity.ok(billingService.createStripeCheckoutSession(userId, tier, interval, req.successUrl(), req.cancelUrl()));
    }

    @GetMapping("/quota")
    public ResponseEntity<UsageQuota> getQuota(
            @RequestParam(value = "userId", required = false, defaultValue = "default-user") String userId
    ) {
        return ResponseEntity.ok(billingService.getUsageQuota(userId));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceRecord>> listInvoices(
            @RequestParam(value = "userId", required = false, defaultValue = "default-user") String userId
    ) {
        return ResponseEntity.ok(billingService.listInvoices(userId));
    }

    @GetMapping("/credits")
    public ResponseEntity<ApiCreditBalance> getCredits(
            @RequestParam(value = "userId", required = false, defaultValue = "default-user") String userId
    ) {
        return ResponseEntity.ok(billingService.getCreditBalance(userId));
    }

    @PostMapping("/credits/topup")
    public ResponseEntity<ApiCreditBalance> topUpCredits(@RequestBody TopUpCreditsRequest req) {
        String userId = req.userId() != null ? req.userId() : "default-user";
        double amount = req.amountUsd() != null ? req.amountUsd() : 50.0;
        return ResponseEntity.ok(billingService.addApiCredits(userId, amount));
    }

    public record UpgradeSubscriptionRequest(String userId, SubscriptionTier tier, BillingInterval interval) {}
    public record StripeCheckoutRequest(String userId, SubscriptionTier tier, BillingInterval interval, String successUrl, String cancelUrl) {}
    public record TopUpCreditsRequest(String userId, Double amountUsd) {}
}
