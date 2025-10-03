package com.leaguehq.dto.response;

import com.leaguehq.model.Venue;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class VenueResponse {

    private UUID id;
    private String name;
    private String address;
    private Instant createdAt;

    public static VenueResponse fromEntity(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .createdAt(venue.getCreatedAt())
                .build();
    }
}
