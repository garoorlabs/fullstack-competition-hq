package com.leaguehq.controller;

import com.leaguehq.dto.request.RegisterTeamRequest;
import com.leaguehq.dto.response.CheckoutSessionResponse;
import com.leaguehq.dto.response.TeamResponse;
import com.leaguehq.model.Team;
import com.leaguehq.security.UserPrincipal;
import com.leaguehq.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<CheckoutSessionResponse> registerTeam(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RegisterTeamRequest request) {

        log.info("Register team request from user: {}, competition: {}",
                userPrincipal.getId(), request.getCompetitionId());

        // Step 1: Create team record
        Team team = teamService.registerTeam(userPrincipal.getId(), request);

        // Step 2: Create Stripe Checkout session
        CheckoutSessionResponse checkoutSession = teamService.createCheckoutSession(team.getId());

        log.info("Team registered, redirecting to checkout: teamId={}, sessionId={}",
                team.getId(), checkoutSession.getSessionId());

        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutSession);
    }

    @GetMapping("/my")
    public ResponseEntity<List<TeamResponse>> getMyTeams(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Get my teams request from user: {}", userPrincipal.getId());

        List<TeamResponse> teams = teamService.getMyTeams(userPrincipal.getId());

        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {

        log.info("Get team request: teamId={}, userId={}", id, userPrincipal.getId());

        TeamResponse team = teamService.getTeamById(id, userPrincipal.getId());

        return ResponseEntity.ok(team);
    }

    @PostMapping("/{id}/update-payment")
    public ResponseEntity<CheckoutSessionResponse> updatePaymentMethod(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {

        log.info("Update payment method request: teamId={}, userId={}", id, userPrincipal.getId());

        CheckoutSessionResponse response = teamService.createCustomerPortalSession(id, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }
}
