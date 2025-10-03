package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_stripe_customer", columnList = "stripe_customer_id"),
    @Index(name = "idx_users_stripe_connect", columnList = "stripe_connect_account_id"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_payout_status", columnList = "payout_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    // Stripe identifiers
    @Column(name = "stripe_customer_id", unique = true, length = 255)
    private String stripeCustomerId;

    @Column(name = "stripe_connect_account_id", unique = true, length = 255)
    private String stripeConnectAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stripe_connect_status", length = 50)
    @Builder.Default
    private StripeConnectStatus stripeConnectStatus = StripeConnectStatus.NOT_STARTED;

    @Column(name = "stripe_connect_onboarded_at")
    private Instant stripeConnectOnboardedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", length = 50)
    @Builder.Default
    private PayoutStatus payoutStatus = PayoutStatus.NONE;

    // Metadata
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public enum UserRole {
        PLATFORM_OWNER,
        COMPETITION_OWNER,
        COACH
    }

    public enum StripeConnectStatus {
        NOT_STARTED,
        INCOMPLETE,
        VERIFIED,
        BLOCKED
    }

    public enum PayoutStatus {
        NONE,
        PENDING,
        ENABLED,
        BLOCKED
    }
}
