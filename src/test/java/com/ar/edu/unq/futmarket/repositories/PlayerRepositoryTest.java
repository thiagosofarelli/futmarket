package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.Position;
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

    @BeforeEach
    void setUp() {
        messi = player("Messi", "Inter Miami", "MLS", Position.FORWARD);
        Player ronaldo = player("Ronaldo", "Al Nassr", "Saudi Pro League", Position.FORWARD);
        Player ramos = player("Ramos", "Inter Miami", "MLS", Position.DEFENDER);
        playerRepository.saveAll(List.of(messi, ronaldo, ramos));
    }

    @Test
    void save_and_findById() {
        Player found = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Messi");
        assertThat(found.getTotalTokens()).isEqualTo(100);
    }

    @Test
    void findByLeague_returnsOnlyMatchingLeague() {
        List<Player> result = playerRepository.findByLeague("MLS");
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ramos");
    }

    @Test
    void findByLeague_unknownLeague_returnsEmpty() {
        assertThat(playerRepository.findByLeague("Premier League")).isEmpty();
    }

    @Test
    void findByTeam_returnsOnlyMatchingTeam() {
        List<Player> result = playerRepository.findByTeam("Inter Miami");
        assertThat(result).hasSize(2);
    }

    @Test
    void findByPosition_returnsForwards() {
        List<Player> forwards = playerRepository.findByPosition(Position.FORWARD);
        assertThat(forwards).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Messi", "Ronaldo");
    }

    @Test
    void findByLeagueAndTeam_narrowsDown() {
        List<Player> result = playerRepository.findByLeagueAndTeam("MLS", "Inter Miami");
        assertThat(result).hasSize(2);
    }

    @Test
    void findByLeagueAndPosition_returnsDefendersInMLS() {
        List<Player> result = playerRepository.findByLeagueAndPosition("MLS", Position.DEFENDER);
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Ramos");
    }

    @Test
    void defaultValues_areCorrect() {
        Player p = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(p.getTotalTokens()).isEqualTo(100);
        assertThat(p.getCurrentValue()).isEqualByComparingTo("1");
    }

    // --- helpers ---

    private Player player(String name, String team, String league, Position position) {
        Player p = new Player();
        p.setName(name);
        p.setTeam(team);
        p.setLeague(league);
        p.setPosition(position);
        return p;
    }
}

