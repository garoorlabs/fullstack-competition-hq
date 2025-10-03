package com.leaguehq.repository;

import com.leaguehq.model.Match;
import com.leaguehq.model.Match.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByCompetitionId(UUID competitionId);

    List<Match> findByCompetitionIdAndStatus(UUID competitionId, MatchStatus status);

    List<Match> findByCompetitionIdAndMatchDate(UUID competitionId, LocalDate matchDate);

    @Query("SELECT m FROM Match m WHERE m.competition.id = :competitionId " +
           "AND (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)")
    List<Match> findByCompetitionIdAndTeamId(UUID competitionId, UUID teamId);

    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId")
    List<Match> findByTeamId(UUID teamId);

    @Query("SELECT m FROM Match m WHERE m.competition.id = :competitionId " +
           "AND m.matchDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.matchDate, m.matchTime")
    List<Match> findByCompetitionIdAndDateRange(UUID competitionId, LocalDate startDate, LocalDate endDate);
}
