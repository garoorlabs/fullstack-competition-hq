package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "matches", indexes = {
    @Index(name = "idx_matches_competition", columnList = "competition_id"),
    @Index(name = "idx_matches_teams", columnList = "home_team_id, away_team_id"),
    @Index(name = "idx_matches_date", columnList = "match_date"),
    @Index(name = "idx_matches_status", columnList = "status"),
    @Index(name = "idx_matches_comp_date_time", columnList = "competition_id, match_date, match_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    // Scheduling
    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "match_time")
    private LocalTime matchTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    // Results (NULL = not entered, 0 = actual zero score)
    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    // Result tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_entered_by")
    private User resultEnteredBy;

    @Column(name = "result_entered_at")
    private Instant resultEnteredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum MatchStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        POSTPONED,
        DISPUTED
    }
}
