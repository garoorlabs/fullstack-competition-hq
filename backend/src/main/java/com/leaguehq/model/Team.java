package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teams",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_teams_competition_name", columnNames = {"competition_id", "name"})
    },
    indexes = {
        @Index(name = "idx_teams_competition", columnList = "competition_id"),
        @Index(name = "idx_teams_coach", columnList = "coach_id"),
        @Index(name = "idx_teams_subscription", columnList = "subscription_id"),
        @Index(name = "idx_teams_eligibility", columnList = "is_eligible"),
        @Index(name = "idx_teams_subscription_status", columnList = "subscription_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @Column(nullable = false, length = 255)
    private String name;

    // Payment tracking
    @Column(name = "entry_fee_paid")
    @Builder.Default
    private Boolean entryFeePaid = false;

    @Column(name = "entry_fee_paid_at")
    private Instant entryFeePaidAt;

    @Column(name = "entry_fee_stripe_payment_id", length = 255)
    private String entryFeeStripePaymentId;

    // Stripe IDs for reconciliation
    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;

    @Column(name = "stripe_latest_invoice_id", length = 255)
    private String stripeLatestInvoiceId;

    @Column(name = "stripe_latest_payment_intent_id", length = 255)
    private String stripeLatestPaymentIntentId;

    // Subscription tracking
    @Column(name = "subscription_id", unique = true, length = 255)
    private String subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 50)
    private SubscriptionStatus subscriptionStatus;

    @Column(name = "subscription_current_period_start")
    private Instant subscriptionCurrentPeriodStart;

    @Column(name = "subscription_current_period_end")
    private Instant subscriptionCurrentPeriodEnd;

    @Column(name = "subscription_cancel_at")
    private Instant subscriptionCancelAt;

    // Eligibility
    @Column(name = "is_eligible")
    @Builder.Default
    private Boolean isEligible = true;

    // Roster status (auto-updated by trigger)
    @Column(name = "roster_size")
    @Builder.Default
    private Integer rosterSize = 0;

    @Column(name = "roster_locked")
    @Builder.Default
    private Boolean rosterLocked = false;

    @Column(name = "roster_locked_at")
    private Instant rosterLockedAt;

    // Metadata
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "registered_at")
    private Instant registeredAt;

    public enum SubscriptionStatus {
        ACTIVE,
        PAST_DUE,
        CANCELLED,
        INCOMPLETE,
        TRIALING
    }
}
