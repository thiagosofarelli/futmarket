package com.ar.edu.unq.futmarket.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findPlayersByLeague(String league);

    List<Player> findPlayersByTeam(String team);

    List<Player> findPlayersByPlayerPosition(PlayerPosition playerPosition);

    List<Player> findPlayersByLeagueAndTeam(String league, String team);

    List<Player> findPlayersByLeagueAndPlayerPosition(String league, PlayerPosition playerPosition);

    List<Player> findPlayersByLeagueAndTeamAndPlayerPosition(String league, String team, PlayerPosition position);

    List<Player> findPlayersByTeamAndPlayerPosition(String team, PlayerPosition position);

    Optional<Player> findPlayerByExternalId(Long externalId);

    @Query("SELECT p FROM Player p WHERE p.league = :leagueName " +
            "AND (p.lastStatsSync IS NULL OR p.lastStatsSync < :threshold)")
    List<Player> findPlayersNeedsStatsSync(@Param("leagueName") String leagueName,
                                      @Param("threshold") LocalDateTime threshold);
}
