package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class QuoteRepositoryTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setName("Messi");
        player.setTeam("Inter Miami");
        player.setLeague("MLS");
        player.setPlayerPosition(PlayerPosition.FORWARD);
        playerRepository.save(player);
    }

    @Test
    void save_persistsQuote() {
        Quote quote = quote(player, new BigDecimal("3.5000"), ValuationStrategy.GENERAL_PERFORMANCE, 3.5);
        Quote saved = quoteRepository.save(quote);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCalculatedAt()).isNotNull();
    }

    @Test
    void findByPlayer_returnsQuotesOrderedByDateDesc() {
        Quote older = quote(player, new BigDecimal("2.0000"), ValuationStrategy.GENERAL_PERFORMANCE, 2.0);
        older.setCalculatedAt(LocalDateTime.now().minusDays(2));

        Quote newer = quote(player, new BigDecimal("4.0000"), ValuationStrategy.POSITION_WEIGHTED, 4.0);
        newer.setCalculatedAt(LocalDateTime.now().minusDays(1));

        quoteRepository.saveAll(List.of(older, newer));

        List<Quote> result = quoteRepository.findByPlayerOrderByCalculatedAtDesc(player);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCurrentTokenPrice()).isEqualByComparingTo("4.0000");
        assertThat(result.get(1).getCurrentTokenPrice()).isEqualByComparingTo("2.0000");
    }

    @Test
    void findByPlayerId_returnsQuotesOrderedByDateDesc() {
        Quote q1 = quote(player, new BigDecimal("1.5000"), ValuationStrategy.GENERAL_PERFORMANCE, 1.5);
        q1.setCalculatedAt(LocalDateTime.now().minusHours(5));

        Quote q2 = quote(player, new BigDecimal("2.5000"), ValuationStrategy.POSITION_WEIGHTED, 2.5);
        q2.setCalculatedAt(LocalDateTime.now());

        quoteRepository.saveAll(List.of(q1, q2));

        List<Quote> result = quoteRepository.findByPlayerIdOrderByCalculatedAtDesc(player.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCurrentTokenPrice()).isEqualByComparingTo("2.5000");
    }

    @Test
    void findByPlayer_noQuotes_returnsEmpty() {
        assertThat(quoteRepository.findByPlayerOrderByCalculatedAtDesc(player)).isEmpty();
    }

    @Test
    void quote_recordsStrategyVersion() {
        Quote q = quote(player, new BigDecimal("5.0000"), ValuationStrategy.POSITION_WEIGHTED, 5.0);
        quoteRepository.save(q);

        Quote found = quoteRepository.findById(q.getId()).orElseThrow();
        assertThat(found.getStrategy()).isEqualTo(ValuationStrategy.POSITION_WEIGHTED);
        assertThat(found.getScore()).isEqualTo(5.0);
    }

    // --- helpers ---

    private Quote quote(Player player, BigDecimal value, ValuationStrategy strategy, double score) {
        Quote q = new Quote();
        q.setPlayer(player);
        q.setCurrentTokenPrice(value);
        q.setStrategy(strategy);
        q.setScore(score);
        return q;
    }
}

