package com.ar.edu.unq.futmarket.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PlayerRepository extends JpaRepository<Player, Long> {

    Page<Player> findPlayersByLeague(String league, Pageable pageable);

    Page<Player> findPlayersByTeam(String team, Pageable pageable);

    Page<Player> findPlayersByPlayerPosition(PlayerPosition playerPosition, Pageable pageable);

    Page<Player> findPlayersByLeagueAndTeam(String league, String team, Pageable pageable);

    Page<Player> findPlayersByLeagueAndPlayerPosition(String league, PlayerPosition playerPosition, Pageable pageable);

    Page<Player> findPlayersByLeagueAndTeamAndPlayerPosition(String league, String team, PlayerPosition position, Pageable pageable);

    Page<Player> findPlayersByTeamAndPlayerPosition(String team, PlayerPosition position, Pageable pageable);

    Optional<Player> findPlayerByExternalId(Long externalId);

    @Query("SELECT p FROM Player p WHERE p.league = :leagueName " +
            "AND (p.lastStatsSync IS NULL OR p.lastStatsSync < :threshold)")
    List<Player> findPlayersNeedsStatsSync(@Param("leagueName") String leagueName,
                                      @Param("threshold") LocalDateTime threshold);
}
