package com.leaguehq.controller;

import com.leaguehq.dto.request.CreateCompetitionRequest;
import com.leaguehq.dto.response.CompetitionResponse;
import com.leaguehq.security.UserPrincipal;
import com.leaguehq.service.CompetitionService;
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
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
@Slf4j
public class CompetitionController {

    private final CompetitionService competitionService;

    @PostMapping
    public ResponseEntity<CompetitionResponse> createCompetition(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCompetitionRequest request) {

        log.info("Create competition request from user: {}", userPrincipal.getId());

        CompetitionResponse response = competitionService.createCompetition(
                userPrincipal.getId(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<CompetitionResponse>> getMyCompetitions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.debug("Get my competitions request from user: {}", userPrincipal.getId());

        List<CompetitionResponse> competitions = competitionService.findByOwner(userPrincipal.getId());

        return ResponseEntity.ok(competitions);
    }

    @GetMapping("/published")
    public ResponseEntity<List<CompetitionResponse>> getPublishedCompetitions() {

        log.debug("Get published competitions request");

        List<CompetitionResponse> competitions = competitionService.findPublishedCompetitions();

        return ResponseEntity.ok(competitions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponse> getCompetition(@PathVariable UUID id) {

        log.debug("Get competition request: {}", id);

        CompetitionResponse response = competitionService.findById(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<CompetitionResponse> publishCompetition(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Publish competition request: competitionId={}, userId={}", id, userPrincipal.getId());

        CompetitionResponse response = competitionService.publishCompetition(id, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/teams")
    public ResponseEntity<List<com.leaguehq.dto.response.TeamResponse>> getCompetitionTeams(
            @PathVariable UUID id) {

        log.debug("Get teams for competition: {}", id);

        List<com.leaguehq.dto.response.TeamResponse> teams = competitionService.getTeamsByCompetitionId(id);

        return ResponseEntity.ok(teams);
    }
}
