package com.leaguehq.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "players",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_players_team_jersey", columnNames = {"team_id", "jersey_number"})
    },
    indexes = {
        @Index(name = "idx_players_team", columnList = "team_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Column(length = 50)
    private String position;

    // Photo (uploaded by coach)
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "photo_uploaded_at")
    private Instant photoUploadedAt;

    @Column(name = "photo_size_bytes")
    private Integer photoSizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
