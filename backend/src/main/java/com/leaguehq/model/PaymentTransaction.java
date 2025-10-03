package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_transactions_team", columnList = "team_id"),
    @Index(name = "idx_transactions_competition", columnList = "competition_id"),
    @Index(name = "idx_transactions_user", columnList = "user_id"),
    @Index(name = "idx_transactions_stripe_payment", columnList = "stripe_payment_intent_id"),
    @Index(name = "idx_transactions_type", columnList = "transaction_type"),
    @Index(name = "idx_transactions_status", columnList = "status"),
    @Index(name = "idx_transactions_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Stripe IDs
    @Column(name = "stripe_payment_intent_id", unique = true, length = 255)
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id", length = 255)
    private String stripeChargeId;

    @Column(name = "stripe_checkout_session_id", length = 255)
    private String stripeCheckoutSessionId;

    // Related entities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Amount (stored in cents)
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "platform_fee_cents")
    @Builder.Default
    private Integer platformFeeCents = 0;

    @Column(name = "net_to_owner_cents")
    @Builder.Default
    private Integer netToOwnerCents = 0;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    // Type
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status;

    // Refund tracking
    @Column(name = "refunded_amount_cents")
    @Builder.Default
    private Integer refundedAmountCents = 0;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "stripe_created_at")
    private Instant stripeCreatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum TransactionType {
        ENTRY_FEE,
        SUBSCRIPTION,
        REFUND
    }

    public enum TransactionStatus {
        PENDING,
        SUCCEEDED,
        FAILED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }
}
