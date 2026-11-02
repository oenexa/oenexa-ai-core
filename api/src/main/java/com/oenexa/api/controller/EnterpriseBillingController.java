package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.billing.BillingDtos.*;
import com.oenexa.api.mapper.BillingApiMapper;
import com.oenexa.application.security.BillingSecurityPolicy;
import com.oenexa.application.service.orchestration.EnterpriseBillingService;
import com.oenexa.application.validation.BillingValidator;
import com.oenexa.domain.model.orchestration.EnterpriseBillingDomain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Enterprise Subscription & Billing REST Controller (Phase 11).
 * Supports standard 11-Layer Clean Hexagonal Architecture:
 * Controller -> DTO -> Validation -> Security -> Mapper -> Service -> ApiResponse<T>.
 */
@RestController
@RequestMapping("/api/v1/billing")
@CrossOrigin(originPatterns = "*")
public class EnterpriseBillingController {

    private final EnterpriseBillingService billingService;
    private final BillingValidator validator;
    private final BillingSecurityPolicy securityPolicy;
    private final BillingApiMapper mapper;

    public EnterpriseBillingController(EnterpriseBillingService billingService) {
        this(billingService, new BillingValidator(), new BillingSecurityPolicy(), new BillingApiMapper());
    }

    @Autowired
    public EnterpriseBillingController(
            EnterpriseBillingService billingService,
            BillingValidator validator,
            BillingSecurityPolicy securityPolicy,
            BillingApiMapper mapper
    ) {
        this.billingService = Objects.requireNonNull(billingService, "billingService must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
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

    // ==========================================
    // 11-Layer Clean Hexagonal Architecture v2 Endpoints
    // ==========================================

    @PostMapping("/v2/subscription/upgrade")
    public ResponseEntity<ApiResponse<SubscriptionStatusDto>> upgradeSubscriptionV2(
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String requesterRole,
            @RequestBody SubscriptionUpgradeRequestDto req
    ) {
        validator.validateUpgradeRequest(req.userId(), req.tier(), req.interval());
        securityPolicy.verifyBillingAccess(requesterId, req.userId(), requesterRole);

        SubscriptionTier tier = SubscriptionTier.valueOf(req.tier().toUpperCase());
        BillingInterval interval = req.interval() != null ? BillingInterval.valueOf(req.interval().toUpperCase()) : BillingInterval.MONTHLY;
        UserSubscription sub = billingService.changeSubscriptionTier(req.userId(), tier, interval);

        return ResponseEntity.ok(ApiResponse.ok("Subscription upgraded successfully", mapper.toSubscriptionStatusDto(sub)));
    }

    @GetMapping("/v2/quota")
    public ResponseEntity<ApiResponse<QuotaUsageDto>> getQuotaV2(
            @RequestParam(value = "userId", required = false, defaultValue = "default-user") String userId,
            @RequestHeader(value = "X-User-Id", defaultValue = "default-user") String requesterId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String requesterRole
    ) {
        securityPolicy.verifyBillingAccess(requesterId, userId, requesterRole);
        UsageQuota quota = billingService.getUsageQuota(userId);
        return ResponseEntity.ok(ApiResponse.ok("Quota retrieved successfully", mapper.toQuotaUsageDto(quota)));
    }

    public record UpgradeSubscriptionRequest(String userId, SubscriptionTier tier, BillingInterval interval) {}
    public record StripeCheckoutRequest(String userId, SubscriptionTier tier, BillingInterval interval, String successUrl, String cancelUrl) {}
    public record TopUpCreditsRequest(String userId, Double amountUsd) {}
}
