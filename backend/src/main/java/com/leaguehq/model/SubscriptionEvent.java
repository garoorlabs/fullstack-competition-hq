package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_events", indexes = {
    @Index(name = "idx_subscription_events_team", columnList = "team_id"),
    @Index(name = "idx_subscription_events_subscription", columnList = "subscription_id"),
    @Index(name = "idx_subscription_events_type", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "subscription_id", nullable = false, length = 255)
    private String subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "stripe_event_id", length = 255)
    private String stripeEventId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum EventType {
        CREATED,
        RENEWED,
        PAYMENT_FAILED,
        CANCELLED,
        PAST_DUE,
        GRACE_PERIOD_STARTED,
        REACTIVATED
    }
}
