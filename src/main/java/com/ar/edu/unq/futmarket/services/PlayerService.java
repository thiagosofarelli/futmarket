package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.adapters.FootballDataClient;
import com.ar.edu.unq.futmarket.adapters.WhoScoredScraper;
import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final WhoScoredScraper whoScoredScraper;
    private final FootballDataClient footballDataClient;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(PlayerNotFoundException::new);
    }

    public List<Player> findByFilters(League league, String team, PlayerPosition position) {
        String leagueName = league != null ? league.getFullName() : null;
        String normalizedTeam = normalizeText(team);

        if (leagueName != null && normalizedTeam != null && position != null) return playerRepository.findPlayersByLeagueAndTeamAndPlayerPosition(leagueName, normalizedTeam, position);
        if (leagueName != null && position != null) return playerRepository.findPlayersByLeagueAndPlayerPosition(leagueName, position);
        if (leagueName != null && normalizedTeam != null) return playerRepository.findPlayersByLeagueAndTeam(leagueName, normalizedTeam);
        if (normalizedTeam != null && position != null) return playerRepository.findPlayersByTeamAndPlayerPosition(normalizedTeam, position);
        if (leagueName != null) return playerRepository.findPlayersByLeague(leagueName);
        if (normalizedTeam != null) return playerRepository.findPlayersByTeam(normalizedTeam);
        if (position != null) return playerRepository.findPlayersByPlayerPosition(position);
        return playerRepository.findAll();
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<Player> getRanking() {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparing(Player::getCurrentTokenPrice).reversed())
                .toList();
    }

    public void syncPlayersStatsByLeague(League league) {
        whoScoredScraper.syncPlayerStats(playerRepository.findPlayersByLeague(league.getFullName()));
    }

    /**
     * Monday night: Premier League
     */
    @Scheduled(cron = "0 0 22 * * 1")
    public void syncPremierLeagueStats() {
        syncPlayersStatsByLeague(League.PL);
    }

    /**
     * Tuesday night: La Liga
     */
    @Scheduled(cron = "0 0 22 * * 2")
    public void syncLaLigaStats() {
        syncPlayersStatsByLeague(League.PD);
    }

    /**
     * Wednesday night: Ligue 1
     */
    @Scheduled(cron = "0 0 22 * * 3")
    public void syncLigue1Stats() {
        syncPlayersStatsByLeague(League.FL1);
    }

    /**
     * Thursday night: Bundesliga
     */
    @Scheduled(cron = "0 0 22 * * 4")
    public void syncBundesligaStats() {
        syncPlayersStatsByLeague(League.BL1);
    }

    /**
     * Friday night: Serie A
     */
    @Scheduled(cron = "0 0 22 * * 5")
    public void syncSerieAStats() {
        syncPlayersStatsByLeague(League.SA);
    }

    /**
     * Daily sync at 22:00 (10 PM)
     */
    @Scheduled(cron = "0 0 22 * * *")
    public void syncPlayers() {
        footballDataClient.syncPlayers();
    }
}
