package com.leaguehq.dto.response;

import com.leaguehq.model.Team;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TeamResponse {

    private UUID id;
    private UUID competitionId;
    private String competitionName;
    private UUID coachId;
    private String coachName;
    private String name;

    // Payment & Subscription
    private Boolean entryFeePaid;
    private Instant entryFeePaidAt;
    private String subscriptionId;
    private Team.SubscriptionStatus subscriptionStatus;
    private Instant subscriptionCurrentPeriodStart;
    private Instant subscriptionCurrentPeriodEnd;
    private Instant subscriptionCancelAt;

    // Roster
    private Boolean isEligible;
    private Integer rosterSize;
    private Boolean rosterLocked;
    private Instant rosterLockedAt;

    // Metadata
    private Instant createdAt;
    private Instant registeredAt;

    public static TeamResponse fromEntity(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .competitionId(team.getCompetition().getId())
                .competitionName(team.getCompetition().getName())
                .coachId(team.getCoach().getId())
                .coachName(team.getCoach().getFullName())
                .name(team.getName())
                .entryFeePaid(team.getEntryFeePaid())
                .entryFeePaidAt(team.getEntryFeePaidAt())
                .subscriptionId(team.getSubscriptionId())
                .subscriptionStatus(team.getSubscriptionStatus())
                .subscriptionCurrentPeriodStart(team.getSubscriptionCurrentPeriodStart())
                .subscriptionCurrentPeriodEnd(team.getSubscriptionCurrentPeriodEnd())
                .subscriptionCancelAt(team.getSubscriptionCancelAt())
                .isEligible(team.getIsEligible())
                .rosterSize(team.getRosterSize())
                .rosterLocked(team.getRosterLocked())
                .rosterLockedAt(team.getRosterLockedAt())
                .createdAt(team.getCreatedAt())
                .registeredAt(team.getRegisteredAt())
                .build();
    }
}
