package com.leaguehq.dto.response;

import com.leaguehq.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String fullName;
    private User.UserRole role;
    private String stripeCustomerId;
    private String stripeConnectAccountId;
    private User.StripeConnectStatus stripeConnectStatus;
    private User.PayoutStatus payoutStatus;
    private Instant createdAt;
    private Instant lastLoginAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .stripeCustomerId(user.getStripeCustomerId())
                .stripeConnectAccountId(user.getStripeConnectAccountId())
                .stripeConnectStatus(user.getStripeConnectStatus())
                .payoutStatus(user.getPayoutStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
