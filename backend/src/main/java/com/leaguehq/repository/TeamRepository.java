package com.leaguehq.repository;

import com.leaguehq.model.Team;
import com.leaguehq.model.Team.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByCompetitionId(UUID competitionId);

    List<Team> findByCoachId(UUID coachId);

    Optional<Team> findBySubscriptionId(String subscriptionId);

    List<Team> findByCompetitionIdAndIsEligible(UUID competitionId, Boolean isEligible);

    boolean existsByCompetitionIdAndName(UUID competitionId, String name);

    @Query("SELECT t FROM Team t WHERE t.subscriptionStatus = :status")
    List<Team> findBySubscriptionStatus(SubscriptionStatus status);

    @Query("SELECT COUNT(t) FROM Team t WHERE t.competition.id = :competitionId AND t.entryFeePaid = true")
    long countRegisteredTeamsByCompetitionId(UUID competitionId);
}
