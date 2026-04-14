package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.EntityNotFoundException;
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
                .orElseThrow(() -> new EntityNotFoundException("Player not found: " + id));
    }

    public List<Player> findByFilters(String league, String team, PlayerPosition position) {
        if (league != null && team != null) return playerRepository.findByLeagueAndTeam(league, team);
        if (league != null && position != null) return playerRepository.findByLeagueAndPlayerPosition(league, position);
        if (league != null) return playerRepository.findByLeague(league);
        if (team != null) return playerRepository.findByTeam(team);
        if (position != null) return playerRepository.findByPlayerPosition(position);
        return playerRepository.findAll();
    }

    public List<Player> getRanking() {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getCurrentTokenPrice).reversed())
                .toList();
    }
}
