package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.services.impl.QuoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class QuoteServiceImplTest {

    @Autowired
    private QuoteServiceImpl quoteService;

    @Autowired
    private PlayerRepository playerRepository;

    Player salah;
    Player martinelli;

    @BeforeEach
    void setUp() {
        salah = playerRepository.save(Player.builder().name("Salah").team("Liverpool").playerPosition(PlayerPosition.FORWARD)
                .goals(10).assists(3).shots(5).keyPasses(1).dribbles(4).tackles(2).interceptions(0).rating(8.0).league(League.PL.getFullName()).build());

        martinelli = playerRepository.save(Player.builder().name("Martinelli").team("Liverpool").playerPosition(PlayerPosition.FORWARD)
                .goals(10).assists(7).shots(9).keyPasses(1).dribbles(4).tackles(2).interceptions(0).rating(8.0).league(League.PL.getFullName()).build());
    }

    @Test
    void findByPlayerId_noQuotes_returnsEmpty() {
        assertThat(quoteService.findByPlayerId(salah.getId())).isEmpty();
    }

    @Test
    void findByPlayerId_afterRecalculation_returnsQuotesDescending() {
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        List<Quote> history = quoteService.findByPlayerId(salah.getId());
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getCalculatedAt())
                .isAfterOrEqualTo(history.get(1).getCalculatedAt());
    }

    @Test
    void recalculateAll_generalPerformance_createsQuoteWithCorrectScore() {
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        Quote quote = quoteService.findByPlayerId(salah.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(5.75, within(0.0001));
        assertThat(quote.getStrategy()).isEqualTo(ValuationStrategy.GENERAL_PERFORMANCE);
    }

    @Test
    void recalculateAll_generalPerformance_updatesPlayerPrice() {
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        Player updated = playerRepository.findById(salah.getId()).orElseThrow();
        assertThat(updated.getCurrentTokenPrice()).isEqualByComparingTo("58.5");
    }

    @Test
    void recalculateAll_positionWeighted_forward_usesCorrectWeights() {
        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(salah.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(6.75, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("68.5");
    }

    @Test
    void recalculateAll_positionWeighted_defender_usesCorrectWeights() {
        Player p = playerRepository.save(Player.builder().name("Ramos").team("Team A").playerPosition(PlayerPosition.DEFENDER)
                .goals(1).assists(0).shots(0).keyPasses(0).dribbles(3).tackles(8).interceptions(6).rating(7.5).league(League.PL.getFullName()).build());
        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(6.1, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("62.0");
    }

    @Test
    void recalculateAll_positionWeighted_midfielder_usesCorrectWeights() {
        Player p = playerRepository.save(Player.builder().name("Modric").team("Team A").playerPosition(PlayerPosition.MIDFIELDER)
                .goals(3).assists(5).shots(0).keyPasses(7).dribbles(6).tackles(4).interceptions(0).rating(8.0).league(League.PL.getFullName()).build());

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(5.65, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("57.5");
    }

    @Test
    void recalculateAll_positionWeighted_goalkeeper_usesCorrectWeights() {
        Player p = playerRepository.save(Player.builder().name("Courtois").team("Team A").playerPosition(PlayerPosition.GOALKEEPER)
                .goals(1).assists(0).shots(0).keyPasses(0).dribbles(3).tackles(8).interceptions(6).rating(7.5).league(League.PL.getFullName()).build());

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(7.3, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("74.000");
    }

    @Test
    void recalculateAll_recordsStrategyOnQuote() {
        Player p = playerRepository.save(Player.builder().name("Messi").team("Team A").playerPosition(PlayerPosition.FORWARD)
                .goals(10).assists(3).shots(5).keyPasses(1).dribbles(4).tackles(2).interceptions(0).rating(8.0).league(League.PL.getFullName()).build());
        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getStrategy()).isEqualTo(ValuationStrategy.POSITION_WEIGHTED);
    }

    @Test
    void recalculateAll_multiplePlayers_createsOneQuoteEach() {
        playerRepository.saveAll(List.of(
                salah,martinelli
        ));

        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        playerRepository.findAll().forEach(p ->
                assertThat(quoteService.findByPlayerId(p.getId())).hasSize(1)
        );
    }
}
