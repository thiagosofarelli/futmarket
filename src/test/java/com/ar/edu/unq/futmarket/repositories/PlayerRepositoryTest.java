package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    private Player messi;
    private Player ronaldo;
    private Player ramos;

    @BeforeEach
    void setUp() {
        messi = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .build();

        ronaldo = Player.builder()
                .name("Ronaldo")
                .team("Al Nassr")
                .league("Saudi Pro League")
                .playerPosition(PlayerPosition.FORWARD)
                .build();

        ramos = Player.builder()
                .name("Ramos")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.DEFENDER)
                .build();

        playerRepository.saveAll(List.of(messi, ronaldo, ramos));
    }

    @Test
    void save_and_findById() {
        Player found = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Messi");
    }

    @Test
    void findByLeague_returnsOnlyMatchingLeague() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> resultPage = playerRepository.findPlayersByLeague("MLS", pageable);
        assertThat(resultPage.getContent()).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByTeam_returnsOnlyMatchingTeam() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> resultPage = playerRepository.findPlayersByTeam("Inter Miami", pageable);
        assertThat(resultPage.getContent()).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByPlayerPosition_returnsForwards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> forwardsPage = playerRepository.findPlayersByPlayerPosition(PlayerPosition.FORWARD, pageable);
        assertThat(forwardsPage.getContent()).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ronaldo");
    }

    @Test
    void findByLeagueAndPlayerPosition_returnsDefendersInMLS() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> resultPage = playerRepository.findPlayersByLeagueAndPlayerPosition("MLS", PlayerPosition.DEFENDER, pageable);
        assertThat(resultPage.getContent()).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Ramos");
    }

    @Test
    void defaultValues_areCorrect() {
        Player p = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(p.getCurrentTokenPrice()).isEqualByComparingTo("1");
    }

    @Test
    void findByLeagueAndTeam_returnsMatchingPlayers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> result = playerRepository.findPlayersByLeagueAndTeam("MLS", "Inter Miami", pageable);
        assertThat(result.getContent()).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByTeamAndPosition_returnsMatchingPlayers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> result = playerRepository.findPlayersByTeamAndPlayerPosition("Inter Miami", PlayerPosition.DEFENDER, pageable);
        assertThat(result.getContent()).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Ramos");
    }

    @Test
    void findByLeagueAndTeamAndPosition_returnsMatchingPlayers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Player> result = playerRepository.findPlayersByLeagueAndTeamAndPlayerPosition(
                "MLS", "Inter Miami", PlayerPosition.FORWARD, pageable);
        assertThat(result.getContent()).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Messi");
    }

    @Test
    void findByExternalId_found_returnsPlayer() {
        messi.setExternalId(42L);
        playerRepository.save(messi);

        assertThat(playerRepository.findPlayerByExternalId(42L))
                .isPresent()
                .map(Player::getName).hasValue("Messi");
    }

    @Test
    void findByExternalId_notFound_returnsEmpty() {
        assertThat(playerRepository.findPlayerByExternalId(-1L)).isEmpty();
    }

    @Test
    void findPlayersNeedsStatsSync_nullLastSync_returnsPlayer() {
        // messi has no lastStatsSync → needs sync
        var threshold = java.time.LocalDate.now().atStartOfDay();
        var result = playerRepository.findPlayersNeedsStatsSync("MLS", threshold);
        assertThat(result).extracting(Player::getName).contains("Messi");
    }

    @Test
    void findPlayersNeedsStatsSync_recentSync_excludesPlayer() {
        messi.setLastStatsSync(java.time.LocalDateTime.now());
        playerRepository.save(messi);

        var threshold = java.time.LocalDate.now().atStartOfDay();
        var result = playerRepository.findPlayersNeedsStatsSync("MLS", threshold);
        assertThat(result).extracting(Player::getName).doesNotContain("Messi");
    }

    @Test
    void findPlayersNeedsStatsSync_outdatedSync_returnsPlayer() {
        messi.setLastStatsSync(java.time.LocalDateTime.now().minusDays(3));
        playerRepository.save(messi);

        var threshold = java.time.LocalDate.now().atStartOfDay();
        var result = playerRepository.findPlayersNeedsStatsSync("MLS", threshold);
        assertThat(result).extracting(Player::getName).contains("Messi");
    }
}
