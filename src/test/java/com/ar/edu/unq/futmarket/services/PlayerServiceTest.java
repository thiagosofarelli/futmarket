package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.EntityNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
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

    private Player messi;
    private Player ronaldo;
    private Player ramos;
    private Player courtois;

    @BeforeEach
    void setUp() {
        messi    = save(player("Messi",    "Inter Miami", "MLS",          PlayerPosition.FORWARD,    "50.00"));
        ronaldo  = save(player("Ronaldo",  "Al Nassr",    "Saudi League", PlayerPosition.FORWARD,    "40.00"));
        ramos    = save(player("Ramos",    "Inter Miami", "MLS",          PlayerPosition.DEFENDER,   "30.00"));
        courtois = save(player("Courtois", "Real Madrid", "La Liga",      PlayerPosition.GOALKEEPER, "20.00"));
    }

    @Test
    void findAll_returnsAllPlayers() {
        assertThat(playerService.findAll()).hasSize(4);
    }

    @Test
    void findById_found_returnsPlayer() {
        Player found = playerService.findById(messi.getId());
        assertThat(found.getName()).isEqualTo("Messi");
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> playerService.findById(-1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findByFilters_noFilters_returnsAll() {
        assertThat(playerService.findByFilters(null, null, null)).hasSize(4);
    }

    @Test
    void findByFilters_byLeague_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters("MLS", null, null);
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByFilters_byTeam_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, "Inter Miami", null);
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByFilters_byPosition_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, null, PlayerPosition.FORWARD);
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ronaldo");
    }

    @Test
    void findByFilters_byLeagueAndTeam_narrowsDown() {
        List<Player> result = playerService.findByFilters("MLS", "Inter Miami", null);
        assertThat(result).hasSize(2);
    }

    @Test
    void findByFilters_byLeagueAndPosition_narrowsDown() {
        List<Player> result = playerService.findByFilters("MLS", null, PlayerPosition.DEFENDER);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Ramos");
    }

    @Test
    void findByFilters_unknownLeague_returnsEmpty() {
        assertThat(playerService.findByFilters("Premier League", null, null)).isEmpty();
    }

    @Test
    void getRanking_returnsSortedByCurrentTokenPriceDesc() {
        List<Player> ranking = playerService.getRanking();
        assertThat(ranking).extracting(Player::getName)
                .containsExactly("Messi", "Ronaldo", "Ramos", "Courtois");
    }

    @Test
    void getRanking_priceUpdated_reflectsNewOrder() {
        courtois.setCurrentTokenPrice(new BigDecimal("999.00"));
        playerRepository.save(courtois);

        List<Player> ranking = playerService.getRanking();
        assertThat(ranking.get(0).getName()).isEqualTo("Courtois");
    }

    private Player player(String name, String team, String league,
                          PlayerPosition position, String price) {
        Player p = new Player(name, team, league, position);
        p.setCurrentTokenPrice(new BigDecimal(price));
        return p;
    }

    private Player save(Player p) {
        return playerRepository.save(p);
    }
}
