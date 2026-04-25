package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    private Player haaland;
    private Player lautaro;
    private Player saliba;
    private Player courtois;

    @BeforeEach
    void setUp() {
        haaland = save(player("Haaland", "Manchester City", League.PL, PlayerPosition.FORWARD, "50.00"));
        lautaro = save(player("Lautaro", "Inter", League.SA, PlayerPosition.FORWARD, "40.00"));
        saliba = save(player("Saliba", "Arsenal FC", League.PL, PlayerPosition.DEFENDER, "30.00"));
        courtois = save(player("Courtois", "Real Madrid", League.PD, PlayerPosition.GOALKEEPER, "20.00"));
    }

    @Test
    void findAll_returnsAllPlayers() {
        assertThat(playerService.findAll()).hasSize(4);
    }

    @Test
    void findById_found_returnsPlayer() {
        Player found = playerService.findById(haaland.getId());
        assertThat(found.getName()).isEqualTo("Haaland");
    }

    @Test
    void findById_notFound_throwsPlayerNotFoundException() {
        assertThatThrownBy(() -> playerService.findById(-1L))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void findByFilters_noFilters_returnsAll() {
        assertThat(playerService.findByFilters(null, null, null)).hasSize(4);
    }

    @Test
    void findByFilters_byLeague_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(League.PL, null, null);
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Haaland", "Saliba");
    }

    @Test
    void findByFilters_byTeam_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, "Inter", null);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Lautaro");
    }

    @Test
    void findByFilters_byPosition_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, null, PlayerPosition.FORWARD);
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Haaland", "Lautaro");
    }

    @Test
    void findByFilters_byLeagueAndTeam_narrowsDown() {
        List<Player> result = playerService.findByFilters(League.PL, "Arsenal FC", null);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Saliba");
    }

    @Test
    void findByFilters_byLeagueAndPosition_narrowsDown() {
        List<Player> result = playerService.findByFilters(League.PL, null, PlayerPosition.DEFENDER);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Saliba");
    }

    @Test
    void findByFilters_unknownLeague_returnsEmpty() {
        assertThat(playerService.findByFilters(League.BL1, null, null)).isEmpty();
    }

    @Test
    void getRanking_returnsSortedByCurrentTokenPriceDesc() {
        List<Player> ranking = playerService.getRanking();
        assertThat(ranking).extracting(Player::getName)
                .containsExactly("Haaland", "Lautaro", "Saliba", "Courtois");
    }

    @Test
    void getRanking_priceUpdated_reflectsNewOrder() {
        courtois.setCurrentTokenPrice(new BigDecimal("999.00"));
        playerRepository.save(courtois);

        List<Player> ranking = playerService.getRanking();
        assertThat(ranking.get(0).getName()).isEqualTo("Courtois");
    }

    private Player player(String name, String team, League league,
                          PlayerPosition position, String price) {
        Player p = new Player(name, team, league.getFullName(), position);
        p.setCurrentTokenPrice(new BigDecimal(price));
        return p;
    }

    private Player save(Player p) {
        return playerRepository.save(p);
    }
}
