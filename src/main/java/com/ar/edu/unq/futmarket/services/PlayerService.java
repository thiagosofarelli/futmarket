package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlayerService {

    public List<Player> findAll();

    public Player findById(Long id);

    public Page<Player> findByFilters(League league, String team, PlayerPosition position, Pageable pageable);

    public List<Player> getRanking();

    public void syncPlayersStatsByLeague(League league);

    public void syncPremierLeagueStats();

    public void syncLaLigaStats();

    public void syncLigue1Stats();

    public void syncBundesligaStats();

    public void syncSerieAStats();

    public void syncPlayers();
}
