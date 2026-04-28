package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        assertThat(found.getAvailableTokens()).isEqualTo(100);
    }

    @Test
    void findByLeague_returnsOnlyMatchingLeague() {
        List<Player> result = playerRepository.findPlayersByLeague("MLS");
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByTeam_returnsOnlyMatchingTeam() {
        List<Player> result = playerRepository.findPlayersByTeam("Inter Miami");
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByPlayerPosition_returnsForwards() {
        List<Player> forwards = playerRepository.findPlayersByPlayerPosition(PlayerPosition.FORWARD);
        assertThat(forwards).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ronaldo");
    }

    @Test
    void findByLeagueAndPlayerPosition_returnsDefendersInMLS() {
        List<Player> result = playerRepository.findPlayersByLeagueAndPlayerPosition("MLS", PlayerPosition.DEFENDER);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Ramos");
    }

    @Test
    void defaultValues_areCorrect() {
        Player p = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(p.getAvailableTokens()).isEqualTo(100);
        assertThat(p.getCurrentTokenPrice()).isEqualByComparingTo("1");
    }
}
