package com.leaguehq.repository;

import com.leaguehq.model.Competition;
import com.leaguehq.model.Competition.CompetitionStatus;
import com.leaguehq.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, UUID> {

    List<Competition> findByOwner(User owner);

    List<Competition> findByOwnerId(UUID ownerId);

    List<Competition> findByStatus(CompetitionStatus status);

    Optional<Competition> findByShareToken(String shareToken);

    List<Competition> findByOwnerIdAndStatus(UUID ownerId, CompetitionStatus status);

    List<Competition> findByOwnerOrderByCreatedAtDesc(User owner);
}
