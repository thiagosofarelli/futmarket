package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(PlayerNotFoundException::new);
    }

    public List<Player> findByFilters(String league, String team, PlayerPosition position) {
        if (league != null && team != null && position != null) return playerRepository.findPlayersByLeagueAndTeamAndPlayerPosition(league, team, position);
        if (league != null && position != null) return playerRepository.findPlayersByLeagueAndPlayerPosition(league, position);
        if (league != null && team != null) return playerRepository.findPlayersByLeagueAndTeam(league, team);
        if (team != null && position != null) return playerRepository.findPlayersByTeamAndPlayerPosition(team, position);
        if (league != null) return playerRepository.findPlayersByLeague(league);
        if (team != null) return playerRepository.findPlayersByTeam(team);
        if (position != null) return playerRepository.findPlayersByPlayerPosition(position);
        return playerRepository.findAll();
    }

    public List<Player> getRanking() {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getCurrentTokenPrice).reversed())
                .toList();
    }
}
