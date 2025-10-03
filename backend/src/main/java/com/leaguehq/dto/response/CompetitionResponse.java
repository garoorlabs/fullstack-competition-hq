package com.leaguehq.dto.response;

import com.leaguehq.model.Competition;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class CompetitionResponse {

    private UUID id;
    private UUID ownerId;
    private String ownerName;
    private String name;
    private String description;
    private Competition.CompetitionFormat format;
    private Competition.TeamSize teamSize;
    private BigDecimal entryFee;
    private Integer currentTeamCount;
    private BigDecimal platformFeePercentage;
    private Map<String, Object> policy;
    private Integer maxTeams;
    private LocalDate registrationDeadline;
    private LocalDate startDate;
    private LocalDate endDate;
    private Competition.CompetitionStatus status;
    private String shareToken;
    private List<VenueResponse> venues;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;

    public static CompetitionResponse fromEntity(Competition competition, List<VenueResponse> venues) {
        return CompetitionResponse.builder()
                .id(competition.getId())
                .ownerId(competition.getOwner().getId())
                .ownerName(competition.getOwner().getFullName())
                .name(competition.getName())
                .description(competition.getDescription())
                .format(competition.getFormat())
                .teamSize(competition.getTeamSize())
                .entryFee(competition.getEntryFee())
                .currentTeamCount(0) // TODO: Calculate from teams table
                .platformFeePercentage(competition.getPlatformFeePercentage())
                .policy(competition.getPolicy())
                .maxTeams(competition.getMaxTeams())
                .registrationDeadline(competition.getRegistrationDeadline())
                .startDate(competition.getStartDate())
                .endDate(competition.getEndDate())
                .status(competition.getStatus())
                .shareToken(competition.getShareToken())
                .venues(venues)
                .createdAt(competition.getCreatedAt())
                .updatedAt(competition.getUpdatedAt())
                .publishedAt(competition.getPublishedAt())
                .build();
    }
}
