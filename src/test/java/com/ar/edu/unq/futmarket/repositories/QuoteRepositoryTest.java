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

    private Player messi;

    @BeforeEach
    void setUp() {
        messi = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .build();

        messi = playerRepository.save(messi);
    }

    @Test
    void save_persistsQuote() {
        Quote quote = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("3.5000"))
                .strategy(ValuationStrategy.GENERAL_PERFORMANCE)
                .score(3.5)
                .build();

        Quote saved = quoteRepository.save(quote);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCalculatedAt()).isNotNull();
    }

    @Test
    void findByPlayer_returnsQuotesOrderedByDateDesc() {
        Quote older = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("2.0000"))
                .strategy(ValuationStrategy.GENERAL_PERFORMANCE)
                .score(2.0)
                .calculatedAt(LocalDateTime.now().minusDays(2))
                .build();

        Quote newer = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("4.0000"))
                .strategy(ValuationStrategy.POSITION_WEIGHTED)
                .score(4.0)
                .calculatedAt(LocalDateTime.now().minusDays(1))
                .build();

        quoteRepository.saveAll(List.of(older, newer));

        List<Quote> result = quoteRepository.findByPlayerOrderByCalculatedAtDesc(messi);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCurrentTokenPrice()).isEqualByComparingTo("4.0000");
        assertThat(result.get(1).getCurrentTokenPrice()).isEqualByComparingTo("2.0000");
    }

    @Test
    void findByPlayerId_returnsQuotesOrderedByDateDesc() {
        Quote q1 = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("1.5000"))
                .strategy(ValuationStrategy.GENERAL_PERFORMANCE)
                .score(1.5)
                .calculatedAt(LocalDateTime.now().minusHours(5))
                .build();

        Quote q2 = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("2.5000"))
                .strategy(ValuationStrategy.POSITION_WEIGHTED)
                .score(2.5)
                .calculatedAt(LocalDateTime.now())
                .build();

        quoteRepository.saveAll(List.of(q1, q2));

        List<Quote> result = quoteRepository.findByPlayerIdOrderByCalculatedAtDesc(messi.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCurrentTokenPrice()).isEqualByComparingTo("2.5000");
    }

    @Test
    void findByPlayer_noQuotes_returnsEmpty() {
        assertThat(quoteRepository.findByPlayerOrderByCalculatedAtDesc(messi)).isEmpty();
    }

    @Test
    void quote_recordsStrategyVersion() {
        Quote q = Quote.builder()
                .player(messi)
                .currentTokenPrice(new BigDecimal("5.0000"))
                .strategy(ValuationStrategy.POSITION_WEIGHTED)
                .score(5.0)
                .build();

        Quote saved = quoteRepository.save(q);

        Quote found = quoteRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getStrategy()).isEqualTo(ValuationStrategy.POSITION_WEIGHTED);
        assertThat(found.getScore()).isEqualTo(5.0);
    }
}

