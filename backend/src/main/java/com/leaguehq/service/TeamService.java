package com.leaguehq.service;

import com.leaguehq.dto.request.RegisterTeamRequest;
import com.leaguehq.dto.response.CheckoutSessionResponse;
import com.leaguehq.dto.response.TeamResponse;
import com.leaguehq.exception.BadRequestException;
import com.leaguehq.exception.ResourceNotFoundException;
import com.leaguehq.model.Competition;
import com.leaguehq.model.Team;
import com.leaguehq.model.User;
import com.leaguehq.repository.CompetitionRepository;
import com.leaguehq.repository.TeamRepository;
import com.leaguehq.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final TeamRepository teamRepository;
    private final CompetitionRepository competitionRepository;
    private final UserRepository userRepository;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String frontendUrl;

    @Value("${stripe.price.monthly-dues}")
    private String monthlyDuesPriceId;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String returnUrl;

    @Transactional
    public Team registerTeam(UUID coachId, RegisterTeamRequest request) {
        log.info("Registering team for coach: {}, competition: {}", coachId, request.getCompetitionId());

        // Validate user is COACH
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (coach.getRole() != User.UserRole.COACH) {
            throw new BadRequestException("Only coaches can register teams");
        }

        // Validate competition exists and is published
        Competition competition = competitionRepository.findById(request.getCompetitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found"));

        if (competition.getStatus() != Competition.CompetitionStatus.PUBLISHED) {
            throw new BadRequestException("Competition is not published yet");
        }

        // Check if registration is still open
        if (competition.getRegistrationDeadline() != null &&
                competition.getRegistrationDeadline().isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Registration deadline has passed");
        }

        // Check if competition is full
        long registeredTeams = teamRepository.countRegisteredTeamsByCompetitionId(request.getCompetitionId());
        if (registeredTeams >= competition.getMaxTeams()) {
            throw new BadRequestException("Competition is full");
        }

        // Check if team name is unique in competition
        if (teamRepository.existsByCompetitionIdAndName(request.getCompetitionId(), request.getTeamName())) {
            throw new BadRequestException("Team name already exists in this competition");
        }

        // Create team
        Team team = Team.builder()
                .competition(competition)
                .coach(coach)
                .name(request.getTeamName())
                .entryFeePaid(false)
                .isEligible(true)
                .rosterSize(0)
                .rosterLocked(false)
                .registeredAt(Instant.now())
                .build();

        Team savedTeam = teamRepository.save(team);
        log.info("Team created: id={}, name={}, competition={}", savedTeam.getId(), savedTeam.getName(), competition.getId());

        return savedTeam;
    }

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(UUID teamId) {
        log.info("Creating checkout session for team: {}", teamId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        if (team.getEntryFeePaid()) {
            throw new BadRequestException("Team has already paid entry fee");
        }

        Competition competition = team.getCompetition();
        BigDecimal entryFee = competition.getEntryFee();

        try {
            String stripeAccountId = competition.getOwner().getStripeConnectAccountId();

            // Create Stripe Checkout Session in SUBSCRIPTION mode
            // This allows combining one-time (entry fee) + recurring (subscription) items
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(frontendUrl + "/teams/registration/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/competitions/" + competition.getId())
                    .putMetadata("team_id", teamId.toString())
                    .putMetadata("competition_id", competition.getId().toString())
                    .putMetadata("team_name", team.getName())
                    .putMetadata("competition_name", competition.getName())
                    // Expand to reduce webhook round-trips
                    .addExpand("subscription")
                    .addExpand("latest_invoice.payment_intent")
                    .addExpand("customer");

            // Add entry fee as ONE-TIME line item (charged immediately on first invoice)
            if (entryFee.compareTo(BigDecimal.ZERO) > 0) {
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(competition.getName() + " - Entry Fee")
                                                                .setDescription("One-time entry fee for " + team.getName())
                                                                .build()
                                                )
                                                .setUnitAmount(entryFee.multiply(new BigDecimal(100)).longValue())
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                );
            }

            // Add $20 RECURRING monthly subscription line item (using pre-created Price)
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setPrice(monthlyDuesPriceId) // Reference pre-created Price
                            .setQuantity(1L)
                            .build()
            );

            // Set up Stripe Connect destination charge with platform fees
            if (stripeAccountId != null) {
                // Calculate platform fee for entry fee (8% of entry fee goes to platform)
                BigDecimal platformFeePercentage = competition.getPlatformFeePercentage();
                long entryFeePlatformFee = entryFee.multiply(platformFeePercentage)
                        .divide(new BigDecimal(100))
                        .multiply(new BigDecimal(100))
                        .longValue();

                // For the one-time entry fee: use application_fee_amount on payment intent
                paramsBuilder.setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setApplicationFeeAmount(entryFeePlatformFee)
                                .setTransferData(
                                        SessionCreateParams.PaymentIntentData.TransferData.builder()
                                                .setDestination(stripeAccountId)
                                                .build()
                                )
                                .build()
                );

                // For the recurring subscription: platform keeps 100% of $20 monthly fee
                // This is handled via subscription_data.application_fee_percent
                paramsBuilder.setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .setApplicationFeePercent(new BigDecimal("100.0")) // Platform keeps 100% of subscription
                                .putMetadata("team_id", teamId.toString())
                                .putMetadata("competition_id", competition.getId().toString())
                                .setTransferData(
                                        SessionCreateParams.SubscriptionData.TransferData.builder()
                                                .setDestination(stripeAccountId)
                                                .setAmountPercent(new BigDecimal("0")) // 0% goes to competition owner for subscription
                                                .build()
                                )
                                .build()
                );
            }

            Session session = Session.create(paramsBuilder.build());

            log.info("Checkout session created: sessionId={}, teamId={}", session.getId(), teamId);

            return CheckoutSessionResponse.builder()
                    .sessionUrl(session.getUrl())
                    .sessionId(session.getId())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to create checkout session: teamId={}, error={}", teamId, e.getMessage(), e);
            throw new BadRequestException("Failed to create checkout session: " + e.getMessage());
        }
    }

    public List<TeamResponse> getMyTeams(UUID coachId) {
        log.info("Getting teams for coach: {}", coachId);

        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (coach.getRole() != User.UserRole.COACH) {
            throw new BadRequestException("Only coaches can view teams");
        }

        List<Team> teams = teamRepository.findByCoachId(coachId);

        return teams.stream()
                .map(TeamResponse::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    public TeamResponse getTeamById(UUID teamId, UUID userId) {
        log.info("Getting team: teamId={}, userId={}", teamId, userId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify access: coach owns the team or user is competition owner
        boolean isCoach = team.getCoach().getId().equals(userId);
        boolean isOwner = team.getCompetition().getOwner().getId().equals(userId);

        if (!isCoach && !isOwner) {
            throw new BadRequestException("You don't have permission to view this team");
        }

        return TeamResponse.fromEntity(team);
    }

    public List<TeamResponse> getTeamsByCompetitionId(UUID competitionId) {
        log.info("Getting teams for competition: {}", competitionId);

        // Verify competition exists
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Competition not found"));

        // Get all teams for this competition
        List<Team> teams = teamRepository.findByCompetitionId(competitionId);

        // Convert to responses and sort by registration date (newest first)
        return teams.stream()
                .map(TeamResponse::fromEntity)
                .sorted((t1, t2) -> {
                    if (t1.getRegisteredAt() == null && t2.getRegisteredAt() == null) return 0;
                    if (t1.getRegisteredAt() == null) return 1;
                    if (t2.getRegisteredAt() == null) return -1;
                    return t2.getRegisteredAt().compareTo(t1.getRegisteredAt());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    public CheckoutSessionResponse createCustomerPortalSession(UUID teamId, UUID coachId) {
        log.info("Creating customer portal session: teamId={}, coachId={}", teamId, coachId);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found"));

        // Verify coach owns this team
        if (!team.getCoach().getId().equals(coachId)) {
            throw new BadRequestException("You don't have permission to update payment for this team");
        }

        // Verify team has a Stripe customer ID
        if (team.getStripeCustomerId() == null || team.getStripeCustomerId().isEmpty()) {
            throw new BadRequestException("No payment method found for this team");
        }

        try {
            // Create Stripe Customer Portal session
            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(team.getStripeCustomerId())
                            .setReturnUrl(frontendUrl + "/teams")
                            .build();

            com.stripe.model.billingportal.Session session =
                    com.stripe.model.billingportal.Session.create(params);

            log.info("Customer portal session created: sessionId={}, teamId={}", session.getId(), teamId);

            return CheckoutSessionResponse.builder()
                    .sessionUrl(session.getUrl())
                    .sessionId(session.getId())
                    .build();

        } catch (com.stripe.exception.StripeException e) {
            log.error("Failed to create customer portal session: teamId={}, error={}", teamId, e.getMessage(), e);
            throw new BadRequestException("Failed to create payment portal session: " + e.getMessage());
        }
    }
}
