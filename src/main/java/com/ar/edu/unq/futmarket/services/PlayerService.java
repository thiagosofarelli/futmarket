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
        String normalizedLeague = normalize(league);
        String normalizedTeam = normalize(team);

        if (normalizedLeague != null && normalizedTeam != null && position != null) return playerRepository.findPlayersByLeagueAndTeamAndPlayerPosition(normalizedLeague, normalizedTeam, position);
        if (normalizedLeague != null && position != null) return playerRepository.findPlayersByLeagueAndPlayerPosition(normalizedLeague, position);
        if (normalizedLeague != null && normalizedTeam != null) return playerRepository.findPlayersByLeagueAndTeam(normalizedLeague, normalizedTeam);
        if (normalizedTeam != null && position != null) return playerRepository.findPlayersByTeamAndPlayerPosition(normalizedTeam, position);
        if (normalizedLeague != null) return playerRepository.findPlayersByLeague(normalizedLeague);
        if (normalizedTeam != null) return playerRepository.findPlayersByTeam(normalizedTeam);
        if (position != null) return playerRepository.findPlayersByPlayerPosition(position);
        return playerRepository.findAll();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<Player> getRanking() {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getCurrentTokenPrice).reversed())
                .toList();
    }
}
