package com.leaguehq.service;

import com.leaguehq.dto.response.ConnectOnboardingLinkResponse;
import com.leaguehq.exception.BadRequestException;
import com.leaguehq.exception.ResourceNotFoundException;
import com.leaguehq.model.*;
import com.leaguehq.repository.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SubscriptionEventRepository subscriptionEventRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API initialized");
    }

    @Transactional
    public ConnectOnboardingLinkResponse createConnectOnboardingLink(UUID userId) {
        log.info("Creating Stripe Connect onboarding link for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify user is COMPETITION_OWNER
        if (user.getRole() != User.UserRole.COMPETITION_OWNER) {
            throw new BadRequestException("Only competition owners can connect Stripe accounts");
        }

        try {
            String accountId;

            // Create or retrieve Stripe Connect account
            if (user.getStripeConnectAccountId() == null) {
                AccountCreateParams createParams = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.STANDARD)
                        .setEmail(user.getEmail())
                        .setCapabilities(
                                AccountCreateParams.Capabilities.builder()
                                        .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                                .setRequested(true)
                                                .build())
                                        .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                                .setRequested(true)
                                                .build())
                                        .build()
                        )
                        .build();

                Account account = Account.create(createParams);
                accountId = account.getId();

                // Update user with Stripe account ID
                user.setStripeConnectAccountId(accountId);
                user.setStripeConnectStatus(User.StripeConnectStatus.INCOMPLETE);
                userRepository.save(user);

                log.info("Created Stripe Connect account: userId={}, accountId={}", userId, accountId);
            } else {
                accountId = user.getStripeConnectAccountId();
                log.info("Using existing Stripe Connect account: userId={}, accountId={}", userId, accountId);
            }

            // Create account link for onboarding
            String returnUrl = frontendUrl + "/dashboard/stripe/return";
            String refreshUrl = frontendUrl + "/dashboard/stripe/refresh";

            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(refreshUrl)
                    .setReturnUrl(returnUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink accountLink = AccountLink.create(linkParams);

            log.info("Created onboarding link: userId={}, expiresAt={}", userId, accountLink.getExpiresAt());

            return ConnectOnboardingLinkResponse.builder()
                    .url(accountLink.getUrl())
                    .expiresAt(accountLink.getExpiresAt())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error creating Connect account: userId={}, error={}", userId, e.getMessage(), e);
            throw new BadRequestException("Failed to create Stripe Connect account: " + e.getMessage());
        }
    }

    @Transactional
    public void handleAccountUpdated(String accountId) {
        log.info("=== WEBHOOK: Processing account.updated webhook: accountId={} ===", accountId);

        User user = userRepository.findByStripeConnectAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for Stripe account: " + accountId));

        log.info("WEBHOOK: Found user: userId={}, email={}, currentPayoutStatus={}",
                user.getId(), user.getEmail(), user.getPayoutStatus());

        try {
            Account account = Account.retrieve(accountId);

            // Check if charges are enabled (account is fully verified)
            boolean chargesEnabled = account.getChargesEnabled();
            boolean payoutsEnabled = account.getPayoutsEnabled();
            boolean detailsSubmitted = account.getDetailsSubmitted();

            log.info("WEBHOOK: Account status: accountId={}, chargesEnabled={}, payoutsEnabled={}, detailsSubmitted={}",
                    accountId, chargesEnabled, payoutsEnabled, detailsSubmitted);

            // Store previous status for comparison
            User.PayoutStatus previousPayoutStatus = user.getPayoutStatus();
            User.StripeConnectStatus previousConnectStatus = user.getStripeConnectStatus();

            // Update user status based on Stripe account state
            if (chargesEnabled && payoutsEnabled) {
                user.setStripeConnectStatus(User.StripeConnectStatus.VERIFIED);
                user.setPayoutStatus(User.PayoutStatus.ENABLED);
                user.setStripeConnectOnboardedAt(Instant.now());
                log.info("WEBHOOK: Setting user account to VERIFIED/ENABLED: userId={}, accountId={}", user.getId(), accountId);
            } else if (detailsSubmitted) {
                user.setStripeConnectStatus(User.StripeConnectStatus.INCOMPLETE);
                user.setPayoutStatus(User.PayoutStatus.PENDING);
                log.info("WEBHOOK: Setting user account to INCOMPLETE/PENDING: userId={}, accountId={}", user.getId(), accountId);
            } else {
                user.setStripeConnectStatus(User.StripeConnectStatus.INCOMPLETE);
                user.setPayoutStatus(User.PayoutStatus.NONE);
                log.info("WEBHOOK: Setting user account to INCOMPLETE/NONE: userId={}, accountId={}", user.getId(), accountId);
            }

            User savedUser = userRepository.save(user);
            log.info("WEBHOOK: ✓ Successfully saved user: userId={}, previousStatus={}->{}, previousPayoutStatus={}->{}",
                    savedUser.getId(),
                    previousConnectStatus, savedUser.getStripeConnectStatus(),
                    previousPayoutStatus, savedUser.getPayoutStatus());

            // Verify the save actually persisted
            User verifyUser = userRepository.findById(user.getId()).orElseThrow();
            log.info("WEBHOOK: ✓ Verified saved user in DB: userId={}, payoutStatus={}, stripeConnectStatus={}",
                    verifyUser.getId(), verifyUser.getPayoutStatus(), verifyUser.getStripeConnectStatus());

        } catch (StripeException e) {
            log.error("WEBHOOK: Failed to retrieve Stripe account: accountId={}, error={}", accountId, e.getMessage(), e);
            throw new RuntimeException("Failed to process account update", e);
        }

        log.info("=== WEBHOOK: Completed processing account.updated for accountId={} ===", accountId);
    }

    @Transactional
    public void refreshAccountStatus(UUID userId) {
        log.info("Manually refreshing account status for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStripeConnectAccountId() == null) {
            log.warn("User has no Stripe Connect account ID: userId={}", userId);
            return;
        }

        String accountId = user.getStripeConnectAccountId();
        log.info("Refreshing Stripe account: userId={}, accountId={}", userId, accountId);

        try {
            Account account = Account.retrieve(accountId);

            boolean chargesEnabled = account.getChargesEnabled();
            boolean payoutsEnabled = account.getPayoutsEnabled();
            boolean detailsSubmitted = account.getDetailsSubmitted();

            log.info("Account status: accountId={}, chargesEnabled={}, payoutsEnabled={}, detailsSubmitted={}",
                    accountId, chargesEnabled, payoutsEnabled, detailsSubmitted);

            User.PayoutStatus previousPayoutStatus = user.getPayoutStatus();
            User.StripeConnectStatus previousConnectStatus = user.getStripeConnectStatus();

            if (chargesEnabled && payoutsEnabled) {
                user.setStripeConnectStatus(User.StripeConnectStatus.VERIFIED);
                user.setPayoutStatus(User.PayoutStatus.ENABLED);
                if (user.getStripeConnectOnboardedAt() == null) {
                    user.setStripeConnectOnboardedAt(Instant.now());
                }
                log.info("Setting user account to VERIFIED/ENABLED: userId={}, accountId={}", user.getId(), accountId);
            } else if (detailsSubmitted) {
                user.setStripeConnectStatus(User.StripeConnectStatus.INCOMPLETE);
                user.setPayoutStatus(User.PayoutStatus.PENDING);
                log.info("Setting user account to INCOMPLETE/PENDING: userId={}, accountId={}", user.getId(), accountId);
            } else {
                user.setStripeConnectStatus(User.StripeConnectStatus.INCOMPLETE);
                user.setPayoutStatus(User.PayoutStatus.NONE);
                log.info("Setting user account to INCOMPLETE/NONE: userId={}, accountId={}", user.getId(), accountId);
            }

            User savedUser = userRepository.save(user);
            log.info("✓ Successfully updated user: userId={}, previousStatus={}->{}, previousPayoutStatus={}->{}",
                    savedUser.getId(),
                    previousConnectStatus, savedUser.getStripeConnectStatus(),
                    previousPayoutStatus, savedUser.getPayoutStatus());

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe account: accountId={}, error={}", accountId, e.getMessage(), e);
            throw new BadRequestException("Failed to refresh account status: " + e.getMessage());
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    // ==================== Payment Webhook Handlers ====================

    @Transactional
    public void handleCheckoutSessionCompleted(String sessionId) {
        log.info("WEBHOOK: Processing checkout.session.completed: sessionId={}", sessionId);

        try {
            // Retrieve the session from Stripe to get all details
            com.stripe.model.checkout.Session session = com.stripe.model.checkout.Session.retrieve(sessionId);

            String teamIdStr = session.getMetadata().get("team_id");
            String competitionIdStr = session.getMetadata().get("competition_id");

            if (teamIdStr == null || competitionIdStr == null) {
                log.error("Missing metadata in checkout session: sessionId={}", sessionId);
                return;
            }

            UUID teamId = UUID.fromString(teamIdStr);
            UUID competitionId = UUID.fromString(competitionIdStr);

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));

            Competition competition = team.getCompetition();
            User coach = team.getCoach();

            // Extract payment details
            String subscriptionId = session.getSubscription();
            String paymentIntentId = session.getPaymentIntent();
            Long amountTotal = session.getAmountTotal(); // in cents

            log.info("Checkout completed: teamId={}, subscriptionId={}, amountTotal={}",
                     teamId, subscriptionId, amountTotal);

            // Update team with subscription info
            team.setEntryFeePaid(true);
            team.setEntryFeePaidAt(Instant.now());
            team.setSubscriptionId(subscriptionId);
            team.setSubscriptionStatus(Team.SubscriptionStatus.ACTIVE);
            team.setIsEligible(true);

            // Get subscription details for period info
            // Note: Period dates will be updated by invoice webhooks
            // if (subscriptionId != null) {
            //     Subscription subscription = Subscription.retrieve(subscriptionId);
            //     team.setSubscriptionCurrentPeriodStart(Instant.ofEpochSecond(subscription.getCurrentPeriodStart()));
            //     team.setSubscriptionCurrentPeriodEnd(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()));
            // }

            teamRepository.save(team);

            // Calculate split amounts
            BigDecimal entryFee = competition.getEntryFee();
            BigDecimal platformFeePercentage = competition.getPlatformFeePercentage();
            long entryFeeCents = entryFee.multiply(new BigDecimal(100)).longValue();
            long subscriptionCents = 2000L; // $20

            long platformFeeFromEntry = entryFee.multiply(platformFeePercentage)
                    .divide(new BigDecimal(100))
                    .multiply(new BigDecimal(100))
                    .longValue();
            long netToOwner = entryFeeCents - platformFeeFromEntry;

            // Create payment transaction for entry fee
            if (entryFeeCents > 0) {
                PaymentTransaction entryFeeTransaction = PaymentTransaction.builder()
                        .stripePaymentIntentId(paymentIntentId)
                        .stripeCheckoutSessionId(sessionId)
                        .team(team)
                        .competition(competition)
                        .user(coach)
                        .amountCents((int) entryFeeCents)
                        .platformFeeCents((int) platformFeeFromEntry)
                        .netToOwnerCents((int) netToOwner)
                        .currency("USD")
                        .transactionType(PaymentTransaction.TransactionType.ENTRY_FEE)
                        .status(PaymentTransaction.TransactionStatus.SUCCEEDED)
                        .stripeCreatedAt(Instant.now())
                        .build();

                paymentTransactionRepository.save(entryFeeTransaction);
                log.info("Created entry fee transaction: teamId={}, amount={}", teamId, entryFeeCents);
            }

            // Create payment transaction for first month subscription
            PaymentTransaction subscriptionTransaction = PaymentTransaction.builder()
                    .stripePaymentIntentId(paymentIntentId)
                    .stripeCheckoutSessionId(sessionId)
                    .team(team)
                    .competition(competition)
                    .user(coach)
                    .amountCents((int) subscriptionCents)
                    .platformFeeCents((int) subscriptionCents) // 100% to platform
                    .netToOwnerCents(0)
                    .currency("USD")
                    .transactionType(PaymentTransaction.TransactionType.SUBSCRIPTION)
                    .status(PaymentTransaction.TransactionStatus.SUCCEEDED)
                    .stripeCreatedAt(Instant.now())
                    .build();

            paymentTransactionRepository.save(subscriptionTransaction);
            log.info("Created subscription transaction: teamId={}, amount={}", teamId, subscriptionCents);

            // Log subscription event
            SubscriptionEvent event = SubscriptionEvent.builder()
                    .team(team)
                    .subscriptionId(subscriptionId)
                    .eventType(SubscriptionEvent.EventType.CREATED)
                    .newStatus("ACTIVE")
                    .build();

            subscriptionEventRepository.save(event);

            log.info("✓ Checkout session completed successfully: teamId={}, subscriptionId={}", teamId, subscriptionId);

        } catch (StripeException e) {
            log.error("Failed to retrieve checkout session: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new BadRequestException("Failed to process checkout session: " + e.getMessage());
        }
    }

    @Transactional
    public void handleInvoicePaymentSucceeded(String invoiceId) {
        log.info("WEBHOOK: Processing invoice.payment_succeeded: invoiceId={}", invoiceId);

        try {
            Invoice invoice = Invoice.retrieve(invoiceId);
            // Get subscription ID from invoice lines
            String subscriptionId = null;
            if (invoice.getLines() != null && invoice.getLines().getData().size() > 0) {
                subscriptionId = invoice.getLines().getData().get(0).getSubscription();
            }

            if (subscriptionId == null) {
                log.warn("Invoice has no subscription: invoiceId={}", invoiceId);
                return;
            }

            // Find team by subscription ID
            Team team = teamRepository.findBySubscriptionId(subscriptionId)
                    .orElse(null);

            if (team == null) {
                log.warn("No team found for subscription: subscriptionId={}", subscriptionId);
                return;
            }

            log.info("Processing successful subscription payment: teamId={}, subscriptionId={}",
                     team.getId(), subscriptionId);

            String oldStatus = team.getSubscriptionStatus() != null ? team.getSubscriptionStatus().name() : null;

            // Update team subscription status
            team.setSubscriptionStatus(Team.SubscriptionStatus.ACTIVE);
            team.setIsEligible(true);
            // Period dates are in invoice lines
            if (invoice.getLines() != null && invoice.getLines().getData().size() > 0) {
                var line = invoice.getLines().getData().get(0);
                if (line.getPeriod() != null) {
                    team.setSubscriptionCurrentPeriodStart(Instant.ofEpochSecond(line.getPeriod().getStart()));
                    team.setSubscriptionCurrentPeriodEnd(Instant.ofEpochSecond(line.getPeriod().getEnd()));
                }
            }

            teamRepository.save(team);

            // Create payment transaction for subscription renewal
            // Try to get payment intent ID from the invoice
            String paymentIntentId = invoice.getId();  // Use invoice ID as fallback
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .stripePaymentIntentId(paymentIntentId)
                    .team(team)
                    .competition(team.getCompetition())
                    .user(team.getCoach())
                    .amountCents(invoice.getAmountPaid().intValue())
                    .platformFeeCents(invoice.getAmountPaid().intValue()) // 100% to platform
                    .netToOwnerCents(0)
                    .currency(invoice.getCurrency().toUpperCase())
                    .transactionType(PaymentTransaction.TransactionType.SUBSCRIPTION)
                    .status(PaymentTransaction.TransactionStatus.SUCCEEDED)
                    .stripeCreatedAt(Instant.ofEpochSecond(invoice.getCreated()))
                    .build();

            paymentTransactionRepository.save(transaction);

            // Log subscription event
            SubscriptionEvent event = SubscriptionEvent.builder()
                    .team(team)
                    .subscriptionId(subscriptionId)
                    .eventType(SubscriptionEvent.EventType.RENEWED)
                    .oldStatus(oldStatus)
                    .newStatus("ACTIVE")
                    .build();

            subscriptionEventRepository.save(event);

            log.info("✓ Subscription payment succeeded: teamId={}, subscriptionId={}", team.getId(), subscriptionId);

        } catch (StripeException e) {
            log.error("Failed to retrieve invoice: invoiceId={}, error={}", invoiceId, e.getMessage(), e);
            throw new BadRequestException("Failed to process invoice: " + e.getMessage());
        }
    }

    @Transactional
    public void handleInvoicePaymentFailed(String invoiceId) {
        log.info("WEBHOOK: Processing invoice.payment_failed: invoiceId={}", invoiceId);

        try {
            Invoice invoice = Invoice.retrieve(invoiceId);
            // Get subscription ID from invoice lines
            String subscriptionId = null;
            if (invoice.getLines() != null && invoice.getLines().getData().size() > 0) {
                subscriptionId = invoice.getLines().getData().get(0).getSubscription();
            }

            if (subscriptionId == null) {
                log.warn("Invoice has no subscription: invoiceId={}", invoiceId);
                return;
            }

            // Find team by subscription ID
            Team team = teamRepository.findBySubscriptionId(subscriptionId)
                    .orElse(null);

            if (team == null) {
                log.warn("No team found for subscription: subscriptionId={}", subscriptionId);
                return;
            }

            log.info("Processing failed subscription payment: teamId={}, subscriptionId={}",
                     team.getId(), subscriptionId);

            String oldStatus = team.getSubscriptionStatus() != null ? team.getSubscriptionStatus().name() : null;

            // Update team subscription status to PAST_DUE
            team.setSubscriptionStatus(Team.SubscriptionStatus.PAST_DUE);
            // Optionally mark team as ineligible (could implement grace period)
            // team.setIsEligible(false);

            teamRepository.save(team);

            // Log subscription event
            SubscriptionEvent event = SubscriptionEvent.builder()
                    .team(team)
                    .subscriptionId(subscriptionId)
                    .eventType(SubscriptionEvent.EventType.PAYMENT_FAILED)
                    .oldStatus(oldStatus)
                    .newStatus("PAST_DUE")
                    .build();

            subscriptionEventRepository.save(event);

            log.warn("⚠ Subscription payment failed: teamId={}, subscriptionId={}", team.getId(), subscriptionId);

        } catch (StripeException e) {
            log.error("Failed to retrieve invoice: invoiceId={}, error={}", invoiceId, e.getMessage(), e);
            throw new BadRequestException("Failed to process failed invoice: " + e.getMessage());
        }
    }
}
