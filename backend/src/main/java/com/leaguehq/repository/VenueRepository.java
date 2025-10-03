package com.leaguehq.repository;

import com.leaguehq.model.Competition;
import com.leaguehq.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {

    List<Venue> findByCompetitionId(UUID competitionId);

    List<Venue> findByCompetition(Competition competition);
}
