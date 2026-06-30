package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.adapters.FootballDataClient;
import com.ar.edu.unq.futmarket.adapters.WhoScoredScraper;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.services.impl.PlayerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PlayerServiceSyncTest {

    @Autowired
    private PlayerServiceImpl playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @MockitoBean
    private WhoScoredScraper whoScoredScraper;

    @MockitoBean
    private FootballDataClient footballDataClient;

    @Test
    void syncPlayersStatsByLeague_noPlayersNeedSync_doesNotCallScraper() {
        playerRepository.save(Player.builder()
                .name("Haaland").team("City").league(League.PL.getFullName())
                .playerPosition(PlayerPosition.FORWARD)
                .lastStatsSync(LocalDateTime.now())
                .build());

        playerService.syncPlayersStatsByLeague(League.PL);

        verify(whoScoredScraper, never()).syncPlayerStats(any());
    }

    @Test
    void syncPlayersStatsByLeague_playersNeedSync_callsScraper() {
        playerRepository.save(Player.builder()
                .name("Salah").team("Liverpool").league(League.PL.getFullName())
                .playerPosition(PlayerPosition.FORWARD)
                .build()); // lastStatsSync null → needs sync

        playerService.syncPlayersStatsByLeague(League.PL);

        verify(whoScoredScraper).syncPlayerStats(argThat((Collection<Player> list) ->
                list.stream().anyMatch(p -> p.getName().equals("Salah"))));
    }

    @Test
    void syncPlayersStatsByLeague_outdatedSync_callsScraper() {
        playerRepository.save(Player.builder()
                .name("Modric").team("Real Madrid").league(League.PD.getFullName())
                .playerPosition(PlayerPosition.MIDFIELDER)
                .lastStatsSync(LocalDateTime.now().minusDays(2)) // outdated
                .build());

        playerService.syncPlayersStatsByLeague(League.PD);

        verify(whoScoredScraper).syncPlayerStats(any());
    }

    @Test
    void syncPlayersStatsForAllLeagues_doesNotThrow() {
        playerService.syncPlayersStatsForAllLeagues();
        // No players → scraper never called
        verify(whoScoredScraper, never()).syncPlayerStats(any());
    }

    @Test
    void syncPlayers_delegatesToFootballDataClient() {
        playerService.syncPlayers();
        verify(footballDataClient).syncPlayers();
    }

    @Test
    void syncPlayersStatsByLeague_emptyLeague_neverCallsScraper() {
        // No players in BL1
        playerService.syncPlayersStatsByLeague(League.BL1);
        verify(whoScoredScraper, never()).syncPlayerStats(any());
    }

    @Test
    void syncPlayersStatsByLeague_mixedSyncStatus_onlySyncsOutdated() {
        playerRepository.saveAll(List.of(
                Player.builder().name("Up-to-date").team("Team A").league(League.SA.getFullName())
                        .playerPosition(PlayerPosition.FORWARD)
                        .lastStatsSync(LocalDateTime.now())
                        .build(),
                Player.builder().name("Needs-sync").team("Team B").league(League.SA.getFullName())
                        .playerPosition(PlayerPosition.MIDFIELDER)
                        .build()
        ));

        playerService.syncPlayersStatsByLeague(League.SA);

        verify(whoScoredScraper).syncPlayerStats(argThat((Collection<Player> list) ->
                list.size() == 1 && list.iterator().next().getName().equals("Needs-sync")));
    }
}
