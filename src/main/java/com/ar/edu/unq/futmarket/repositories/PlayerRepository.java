package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByLeague(String league);

    List<Player> findByTeam(String team);

    List<Player> findByPosition(Position position);

    List<Player> findByLeagueAndTeam(String league, String team);

    List<Player> findByLeagueAndPosition(String league, Position position);
}

