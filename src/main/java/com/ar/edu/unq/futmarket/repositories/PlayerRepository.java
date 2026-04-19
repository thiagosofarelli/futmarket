package com.ar.edu.unq.futmarket.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findPlayerByLeague(String league);

    List<Player> findPlayerByTeam(String team);

    List<Player> findPlayerByPlayerPosition(PlayerPosition playerPosition);

    List<Player> findPlayerByLeagueAndTeam(String league, String team);

    List<Player> findPlayerByLeagueAndPlayerPosition(String league, PlayerPosition playerPosition);

    List<Player> findPlayerByLeagueAndTeamAndPlayerPosition(String league, String team, PlayerPosition position);

    List<Player> findPlayerByTeamAndPlayerPosition(String team, PlayerPosition position);

    Optional<Player> findByExternalId(Long externalId);
}
