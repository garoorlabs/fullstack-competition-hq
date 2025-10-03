package com.leaguehq.controller;

import com.leaguehq.dto.response.ConnectOnboardingLinkResponse;
import com.leaguehq.security.UserPrincipal;
import com.leaguehq.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/connect-onboarding-link")
    public ResponseEntity<ConnectOnboardingLinkResponse> createConnectOnboardingLink(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Connect onboarding link request from user: {}", userPrincipal.getId());

        ConnectOnboardingLinkResponse response = stripeService.createConnectOnboardingLink(
                userPrincipal.getId()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-account-status")
    public ResponseEntity<Void> refreshAccountStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Refresh account status request from user: {}", userPrincipal.getId());

        stripeService.refreshAccountStatus(userPrincipal.getId());

        return ResponseEntity.ok().build();
    }
}
