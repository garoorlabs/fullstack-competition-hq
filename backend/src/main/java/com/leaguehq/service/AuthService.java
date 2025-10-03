package com.leaguehq.service;

import com.leaguehq.dto.request.LoginRequest;
import com.leaguehq.dto.request.SignupRequest;
import com.leaguehq.dto.response.AuthResponse;
import com.leaguehq.dto.response.UserResponse;
import com.leaguehq.model.User;
import com.leaguehq.exception.BadRequestException;
import com.leaguehq.repository.UserRepository;
import com.leaguehq.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup attempt with existing email: {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .build();

        user = userRepository.save(user);
        log.info("User registered: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

        String token = tokenProvider.generateToken(user.getId());
        UserResponse userResponse = UserResponse.fromEntity(user);

        return new AuthResponse(token, userResponse);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("User logged in: userId={}, email={}", user.getId(), user.getEmail());

        UserResponse userResponse = UserResponse.fromEntity(user);

        return new AuthResponse(token, userResponse);
    }
}
