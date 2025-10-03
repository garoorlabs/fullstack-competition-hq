package com.leaguehq.controller;

import com.leaguehq.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook");

        Event event;

        try {
            // Verify webhook signature
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    stripeService.getWebhookSecret()
            );
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            log.error("Error parsing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        }

        log.info("Webhook event: type={}, id={}", event.getType(), event.getId());

        // Handle the event
        try {
            switch (event.getType()) {
                case "account.updated":
                    handleAccountUpdated(event);
                    break;

                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;

                case "invoice.payment_succeeded":
                    handleInvoicePaymentSucceeded(event);
                    break;

                case "invoice.payment_failed":
                    handleInvoicePaymentFailed(event);
                    break;

                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("Error processing webhook: type={}, id={}, error={}",
                    event.getType(), event.getId(), e.getMessage(), e);
            // Return 200 to avoid Stripe retries for application errors
            return ResponseEntity.ok("Webhook received but processing failed");
        }
    }

    private void handleAccountUpdated(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        String accountId = null;

        // Try to deserialize the object first
        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.Account) {
                com.stripe.model.Account account = (com.stripe.model.Account) stripeObject;
                accountId = account.getId();
                log.info("Successfully deserialized account: accountId={}", accountId);
            }
        }

        // If deserialization failed, extract from event data
        if (accountId == null && event.getData() != null && event.getData().getObject() != null) {
            try {
                StripeObject obj = event.getData().getObject();
                if (obj instanceof com.stripe.model.Account) {
                    accountId = ((com.stripe.model.Account) obj).getId();
                    log.info("Extracted account ID from event.getData(): {}", accountId);
                }
            } catch (Exception e) {
                log.error("Failed to extract account from event.getData(): {}", e.getMessage());
            }
        }

        if (accountId != null) {
            log.info("Processing account.updated: accountId={}", accountId);
            stripeService.handleAccountUpdated(accountId);
        } else {
            log.error("Unable to extract account ID from account.updated event");
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        String sessionId = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.checkout.Session) {
                com.stripe.model.checkout.Session session = (com.stripe.model.checkout.Session) stripeObject;
                sessionId = session.getId();
                log.info("Successfully deserialized checkout session: sessionId={}", sessionId);
            }
        }

        if (sessionId != null) {
            log.info("Processing checkout.session.completed: sessionId={}", sessionId);
            stripeService.handleCheckoutSessionCompleted(sessionId);
        } else {
            log.error("Unable to extract session ID from checkout.session.completed event");
        }
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        String invoiceId = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.Invoice) {
                com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) stripeObject;
                invoiceId = invoice.getId();
                log.info("Successfully deserialized invoice: invoiceId={}", invoiceId);
            }
        }

        if (invoiceId != null) {
            log.info("Processing invoice.payment_succeeded: invoiceId={}", invoiceId);
            stripeService.handleInvoicePaymentSucceeded(invoiceId);
        } else {
            log.error("Unable to extract invoice ID from invoice.payment_succeeded event");
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        String invoiceId = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            if (stripeObject instanceof com.stripe.model.Invoice) {
                com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) stripeObject;
                invoiceId = invoice.getId();
                log.info("Successfully deserialized invoice: invoiceId={}", invoiceId);
            }
        }

        if (invoiceId != null) {
            log.info("Processing invoice.payment_failed: invoiceId={}", invoiceId);
            stripeService.handleInvoicePaymentFailed(invoiceId);
        } else {
            log.error("Unable to extract invoice ID from invoice.payment_failed event");
        }
    }
}
