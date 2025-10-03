package com.leaguehq.service;

import com.leaguehq.dto.request.CreateCompetitionRequest;
import com.leaguehq.dto.response.CompetitionResponse;
import com.leaguehq.dto.response.VenueResponse;
import com.leaguehq.exception.BadRequestException;
import com.leaguehq.exception.ResourceNotFoundException;
import com.leaguehq.model.Competition;
import com.leaguehq.model.User;
import com.leaguehq.model.Venue;
import com.leaguehq.repository.CompetitionRepository;
import com.leaguehq.repository.UserRepository;
import com.leaguehq.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SHARE_TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Transactional
    public CompetitionResponse createCompetition(UUID ownerId, CreateCompetitionRequest request) {
        log.info("Creating competition for owner: {}", ownerId);

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Find owner
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify owner is COMPETITION_OWNER
        if (owner.getRole() != User.UserRole.COMPETITION_OWNER) {
            throw new BadRequestException("Only competition owners can create competitions");
        }

        // Set default policy if not provided
        Map<String, Object> policy = request.getPolicy();
        if (policy == null || policy.isEmpty()) {
            policy = getDefaultPolicy();
        }

        // Generate share token (22+ characters)
        String shareToken = generateShareToken();

        // Create competition
        Competition competition = Competition.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .format(request.getFormat())
                .teamSize(request.getTeamSize())
                .entryFee(request.getEntryFee())
                .policy(policy)
                .maxTeams(request.getMaxTeams())
                .registrationDeadline(request.getRegistrationDeadline())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Competition.CompetitionStatus.DRAFT)
                .shareToken(shareToken)
                .build();

        competition = competitionRepository.save(competition);
        log.info("Competition created: competitionId={}, shareToken={}", competition.getId(), shareToken);

        // Create venue
        Venue venue = Venue.builder()
                .competition(competition)
                .name(request.getVenue().getName())
                .address(request.getVenue().getAddress())
                .build();

        venue = venueRepository.save(venue);
        log.info("Venue created: venueId={}, competitionId={}", venue.getId(), competition.getId());

        List<VenueResponse> venues = List.of(VenueResponse.fromEntity(venue));
        return CompetitionResponse.fromEntity(competition, venues);
    }

    @Transactional(readOnly = true)
    public List<CompetitionResponse> findByOwner(UUID ownerId) {
        log.debug("Finding competitions for owner: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Competition> competitions = competitionRepository.findByOwnerOrderByCreatedAtDesc(owner);

        return competitions.stream()
                .map(competition -> {
                    List<Venue> venues = venueRepository.findByCompetition(competition);
                    List<VenueResponse> venueResponses = venues.stream()
                            .map(VenueResponse::fromEntity)
                            .collect(Collectors.toList());
                    return CompetitionResponse.fromEntity(competition, venueResponses);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompetitionResponse findById(UUID competitionId) {
        log.debug("Finding competition: {}", competitionId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found"));

        List<Venue> venues = venueRepository.findByCompetition(competition);
        List<VenueResponse> venueResponses = venues.stream()
                .map(VenueResponse::fromEntity)
                .collect(Collectors.toList());

        return CompetitionResponse.fromEntity(competition, venueResponses);
    }

    private String generateShareToken() {
        StringBuilder token = new StringBuilder(24);
        for (int i = 0; i < 24; i++) {
            token.append(SHARE_TOKEN_CHARS.charAt(RANDOM.nextInt(SHARE_TOKEN_CHARS.length())));
        }
        return token.toString();
    }

    public List<CompetitionResponse> findPublishedCompetitions() {
        log.info("Finding all published competitions");

        List<Competition> competitions = competitionRepository.findAll().stream()
                .filter(c -> c.getStatus() == Competition.CompetitionStatus.PUBLISHED)
                .collect(Collectors.toList());

        return competitions.stream()
                .map(competition -> {
                    List<Venue> venues = venueRepository.findByCompetition(competition);
                    List<VenueResponse> venueResponses = venues.stream()
                            .map(VenueResponse::fromEntity)
                            .collect(Collectors.toList());
                    return CompetitionResponse.fromEntity(competition, venueResponses);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CompetitionResponse publishCompetition(UUID competitionId, UUID ownerId) {
        log.info("Publishing competition: competitionId={}, ownerId={}", competitionId, ownerId);

        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found"));

        // Verify ownership
        if (!competition.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You can only publish your own competitions");
        }

        // Check if owner has Stripe Connect enabled
        User owner = competition.getOwner();
        if (owner.getPayoutStatus() != User.PayoutStatus.ENABLED) {
            throw new BadRequestException("Cannot publish competition. You must complete Stripe Connect onboarding first.");
        }

        // Update status to PUBLISHED
        competition.setStatus(Competition.CompetitionStatus.PUBLISHED);
        competition.setPublishedAt(Instant.now());
        competition = competitionRepository.save(competition);

        log.info("Competition published: competitionId={}, status={}", competitionId, competition.getStatus());

        List<Venue> venues = venueRepository.findByCompetition(competition);
        List<VenueResponse> venueResponses = venues.stream()
                .map(VenueResponse::fromEntity)
                .collect(Collectors.toList());

        return CompetitionResponse.fromEntity(competition, venueResponses);
    }

    private Map<String, Object> getDefaultPolicy() {
        // Default policy matching db_schema.md exactly
        Map<String, Object> policy = new HashMap<>();

        // Scoring
        Map<String, Integer> scoring = new HashMap<>();
        scoring.put("win", 3);
        scoring.put("draw", 1);
        scoring.put("loss", 0);
        policy.put("scoring", scoring);

        // Tiebreakers
        policy.put("tiebreakers", List.of("goal_diff", "goals_for", "head_to_head"));

        // Roster
        Map<String, Object> roster = new HashMap<>();
        roster.put("min_size", 8);
        roster.put("max_size", 20);
        roster.put("lock_at", "competition_start");
        policy.put("roster", roster);

        // Refunds
        Map<String, Object> refunds = new HashMap<>();
        refunds.put("full_refund_days_before", 14);
        refunds.put("partial_refund_days_before", 7);
        refunds.put("partial_refund_percentage", 50);
        policy.put("refunds", refunds);

        return policy;
    }
}
