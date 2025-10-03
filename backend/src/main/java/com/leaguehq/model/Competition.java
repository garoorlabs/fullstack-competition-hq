package com.leaguehq.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "competitions", indexes = {
    @Index(name = "idx_competitions_owner", columnList = "owner_id"),
    @Index(name = "idx_competitions_status", columnList = "status"),
    @Index(name = "idx_competitions_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_competitions_share_token", columnList = "share_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Basic info
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CompetitionFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "team_size", nullable = false, length = 50)
    private TeamSize teamSize;

    // Financial
    @Column(name = "entry_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal entryFee;

    @Column(name = "platform_fee_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal platformFeePercentage = BigDecimal.valueOf(8.00);

    // Competition policy (JSONB)
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> policy;

    // Capacity
    @Column(name = "max_teams", nullable = false)
    private Integer maxTeams;

    // Dates
    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private CompetitionStatus status = CompetitionStatus.DRAFT;

    // Shareable registration link
    @Column(name = "share_token", unique = true, length = 64)
    private String shareToken;

    // Metadata
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public enum CompetitionStatus {
        DRAFT,
        PUBLISHED,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public enum CompetitionFormat {
        LEAGUE,
        KNOCKOUT,
        ROUND_ROBIN
    }

    public enum TeamSize {
        FIVE_V_FIVE,
        SIX_V_SIX,
        SEVEN_V_SEVEN,
        EIGHT_V_EIGHT,
        NINE_V_NINE,
        ELEVEN_V_ELEVEN
    }
}
