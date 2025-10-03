package com.leaguehq.repository;

import com.leaguehq.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    List<Player> findByTeamId(UUID teamId);

    Optional<Player> findByTeamIdAndJerseyNumber(UUID teamId, Integer jerseyNumber);

    boolean existsByTeamIdAndJerseyNumber(UUID teamId, Integer jerseyNumber);

    long countByTeamId(UUID teamId);
}
