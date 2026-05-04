package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.adapters.FootballDataClient;
import com.ar.edu.unq.futmarket.adapters.WhoScoredScraper;
import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final WhoScoredScraper whoScoredScraper;
    private final FootballDataClient footballDataClient;
    private final ApplicationContext applicationContext;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(PlayerNotFoundException::new);
    }

    public Page<Player> findByFilters(League league, String team, PlayerPosition position, Pageable pageable) {
        String leagueName = league != null ? league.getFullName() : null;
        String normalizedTeam = normalizeText(team);

        if (leagueName != null && normalizedTeam != null && position != null) return playerRepository.findPlayersByLeagueAndTeamAndPlayerPosition(leagueName, normalizedTeam, position, pageable);
        if (leagueName != null && position != null) return playerRepository.findPlayersByLeagueAndPlayerPosition(leagueName, position, pageable);
        if (leagueName != null && normalizedTeam != null) return playerRepository.findPlayersByLeagueAndTeam(leagueName, normalizedTeam, pageable);
        if (normalizedTeam != null && position != null) return playerRepository.findPlayersByTeamAndPlayerPosition(normalizedTeam, position, pageable);
        if (leagueName != null) return playerRepository.findPlayersByLeague(leagueName, pageable);
        if (normalizedTeam != null) return playerRepository.findPlayersByTeam(normalizedTeam, pageable);
        if (position != null) return playerRepository.findPlayersByPlayerPosition(position, pageable);
        return playerRepository.findAll(pageable);
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
        // Umbral: Hoy a las 00:00:00
        LocalDateTime threshold = LocalDate.now().atStartOfDay();

        List<Player> playersToSync = playerRepository.findPlayersNeedsSync(
                league.getFullName(),
                threshold
        );

        if (!playersToSync.isEmpty()) {
            whoScoredScraper.syncPlayerStats(playersToSync);
        } else {
            log.info("Todos los jugadores de {} ya están actualizados para el día de hoy");
        }
    }

    public void syncPlayersStatsForAllLeagues() {
        PlayerService proxy = applicationContext.getBean(PlayerService.class);
        proxy.syncLigue1Stats();
        proxy.syncPremierLeagueStats();
        proxy.syncBundesligaStats();
        proxy.syncLaLigaStats();
        proxy.syncSerieAStats();
    }

    @Scheduled(cron = "0 0 22 * * 2")
    @Async
    public void syncPremierLeagueStats() {
        syncPlayersStatsByLeague(League.PL);
    }

    @Scheduled(cron = "0 0 22 * * 3")
    @Async
    public void syncLaLigaStats() {
        syncPlayersStatsByLeague(League.PD);
    }

    @Scheduled(cron = "0 0 22 * * 4")
    @Async
    public void syncLigue1Stats() {
        syncPlayersStatsByLeague(League.FL1);
    }

    @Scheduled(cron = "0 0 22 * * 5")
    @Async
    public void syncBundesligaStats() {
        syncPlayersStatsByLeague(League.BL1);
    }

    @Scheduled(cron = "0 0 22 * * 6")
    @Async
    public void syncSerieAStats() {
        syncPlayersStatsByLeague(League.SA);
    }

    @Scheduled(cron = "0 0 22 * * 1")
    public void syncPlayers() {
        footballDataClient.syncPlayers();
    }
}