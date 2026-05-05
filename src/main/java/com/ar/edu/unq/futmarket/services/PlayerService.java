package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayerService {

    List<Player> findAll();

    Player findById(Long id);

    Page<Player> findByFilters(League league, String team, PlayerPosition position, Pageable pageable);

    List<Player> getRanking();

    void syncPlayersStatsByLeague(League league);

    void syncPlayersStatsForAllLeagues();

    void syncPremierLeagueStats();

    void syncLaLigaStats();

    void syncLigue1Stats();

    void syncBundesligaStats();

    void syncSerieAStats();

    void syncPlayers();
}
