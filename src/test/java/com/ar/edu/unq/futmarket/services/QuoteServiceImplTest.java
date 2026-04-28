package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.services.impl.QuoteServiceImpl;
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

    @Test
    void findByPlayerId_noQuotes_returnsEmpty() {
        Player p = playerRepository.save(player("Messi", PlayerPosition.FORWARD));
        assertThat(quoteService.findByPlayerId(p.getId())).isEmpty();
    }

    @Test
    void findByPlayerId_afterRecalculation_returnsQuotesDescending() {
        Player p = playerRepository.save(playerWithStats("Messi", PlayerPosition.FORWARD,
                10, 3, 5, 1, 4, 2, 0, 8.0));

        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        List<Quote> history = quoteService.findByPlayerId(p.getId());
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getCalculatedAt())
                .isAfterOrEqualTo(history.get(1).getCalculatedAt());
    }

    @Test
    void recalculateAll_generalPerformance_createsQuoteWithCorrectScore() {
        Player p = playerRepository.save(playerWithStats("Messi", PlayerPosition.FORWARD,
                10, 3, 5, 1, 4, 2, 0, 8.0));

        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(5.75, within(0.0001));
        assertThat(quote.getStrategy()).isEqualTo(ValuationStrategy.GENERAL_PERFORMANCE);
    }

    @Test
    void recalculateAll_generalPerformance_updatesPlayerPrice() {
        Player p = playerRepository.save(playerWithStats("Messi", PlayerPosition.FORWARD,
                10, 3, 5, 1, 4, 2, 0, 8.0));

        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        Player updated = playerRepository.findById(p.getId()).orElseThrow();
        assertThat(updated.getCurrentTokenPrice()).isEqualByComparingTo("58.5");
    }

    @Test
    void recalculateAll_positionWeighted_forward_usesCorrectWeights() {
        Player p = playerRepository.save(playerWithStats("Messi", PlayerPosition.FORWARD,
                10, 3, 5, 0, 4, 0, 0, 8.0));

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(6.75, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("68.5");
    }

    @Test
    void recalculateAll_positionWeighted_defender_usesCorrectWeights() {
        Player p = playerRepository.save(playerWithStats("Ramos", PlayerPosition.DEFENDER,
                1, 0, 0, 0, 3, 8, 6, 7.5));

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(6.1, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("62.0");
    }

    @Test
    void recalculateAll_positionWeighted_midfielder_usesCorrectWeights() {
        Player p = playerRepository.save(playerWithStats("Modric", PlayerPosition.MIDFIELDER,
                3, 5, 0, 7, 6, 4, 0, 8.0));

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(5.65, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("57.5");
    }

    @Test
    void recalculateAll_positionWeighted_goalkeeper_usesCorrectWeights() {
        Player p = playerRepository.save(playerWithStats("Courtois", PlayerPosition.GOALKEEPER,
                0, 0, 0, 0, 0, 3, 4, 9.0));

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getScore()).isCloseTo(6.8, within(0.0001));
        assertThat(quote.getCurrentTokenPrice()).isEqualByComparingTo("69.0");
    }

    @Test
    void recalculateAll_recordsStrategyOnQuote() {
        Player p = playerRepository.save(player("Messi", PlayerPosition.FORWARD));

        quoteService.recalculateAll(ValuationStrategy.POSITION_WEIGHTED);

        Quote quote = quoteService.findByPlayerId(p.getId()).get(0);
        assertThat(quote.getStrategy()).isEqualTo(ValuationStrategy.POSITION_WEIGHTED);
    }

    @Test
    void recalculateAll_multiplePlayers_createsOneQuoteEach() {
        playerRepository.saveAll(List.of(
                player("Messi",    PlayerPosition.FORWARD),
                player("Ramos",    PlayerPosition.DEFENDER),
                player("Courtois", PlayerPosition.GOALKEEPER)
        ));

        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);

        playerRepository.findAll().forEach(p ->
                assertThat(quoteService.findByPlayerId(p.getId())).hasSize(1)
        );
    }

    private Player player(String name, PlayerPosition position) {
        return new Player(name, "Team A", "League A", position);
    }

    private Player playerWithStats(String name, PlayerPosition position,
                                   double goals, double assists, double shots,
                                   double keyPasses, double dribbles, double tackles,
                                   double interceptions, double rating) {
        Player p = new Player(name, "Team A", "League A", position);
        p.setGoals(goals);
        p.setAssists(assists);
        p.setShots(shots);
        p.setKeyPasses(keyPasses);
        p.setDribbles(dribbles);
        p.setTackles(tackles);
        p.setInterceptions(interceptions);
        p.setRating(rating);
        return p;
    }
}
