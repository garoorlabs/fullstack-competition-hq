package com.leaguehq.dto.request;

import com.leaguehq.model.Competition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class CreateCompetitionRequest {

    @NotBlank(message = "Competition name is required")
    @Size(max = 255, message = "Competition name must not exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Format is required")
    private Competition.CompetitionFormat format;

    @NotNull(message = "Team size is required")
    private Competition.TeamSize teamSize;

    @NotNull(message = "Entry fee is required")
    @Min(value = 0, message = "Entry fee must be non-negative")
    private BigDecimal entryFee;

    private Map<String, Object> policy;

    @NotNull(message = "Maximum number of teams is required")
    @Min(value = 2, message = "Must allow at least 2 teams")
    private Integer maxTeams;

    private LocalDate registrationDeadline;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // Venue info (single venue for MVP)
    @Valid
    @NotNull(message = "Venue is required")
    private VenueRequest venue;

    @Data
    public static class VenueRequest {
        @NotBlank(message = "Venue name is required")
        private String name;

        @NotBlank(message = "Address is required")
        private String address;
    }
}
