package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.QuoteRepository;
import com.ar.edu.unq.futmarket.services.PlayerService;
import com.ar.edu.unq.futmarket.services.QuoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final PlayerRepository playerRepository;
    private final ApplicationContext applicationContext;

    @Value("${futmarket.valuation.base-value:1.0}")
    private double baseValue;

    @Value("${futmarket.valuation.scale-factor:10.0}")
    private double scaleFactor;

    public List<Quote> findByPlayerId(Long playerId) {
        return quoteRepository.findByPlayerIdOrderByCalculatedAtDesc(playerId);
    }

    // 1. El Scheduled NO es transaccional (para no bloquear la DB horas)
    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Argentina/Buenos_Aires")
    public void scheduleWeeklyRecalculation() {
        ValuationStrategy strategy = ValuationStrategy.GENERAL_PERFORMANCE;
        this.recalculateAll(strategy);
    }

    public void recalculateAll(ValuationStrategy strategy) {
        List<Player> players = playerRepository.findAll();
        QuoteService proxy = applicationContext.getBean(QuoteService.class);


        for (Player player : players) {
            try {
                proxy.recalculateSinglePlayer(player, strategy);
            } catch (Exception e) {
                log.error("Failed recauculating player {}: {}", player.getName(), e.getMessage());
            }
        }
        log.info("Recalculated succesfully.");
    }

    @Transactional
    public void recalculateSinglePlayer(Player player, ValuationStrategy strategy) {
        double score = calculateScore(player, strategy);

        BigDecimal newPrice = BigDecimal.valueOf(baseValue + score * scaleFactor)
                .setScale(4, RoundingMode.HALF_UP);

        // Update player price
        player.setCurrentTokenPrice(newPrice);
        playerRepository.save(player);

        // Create quote record
        Quote quote = new Quote();
        quote.setPlayer(player);
        quote.setCurrentTokenPrice(newPrice);
        quote.setStrategy(strategy);
        quote.setScore(score);
        quoteRepository.save(quote);
    }

    private double calculateScore(Player player, ValuationStrategy strategy) {
        return switch (strategy) {
            case GENERAL_PERFORMANCE -> calculateGeneralPerformance(player);
            case POSITION_WEIGHTED -> calculatePositionWeighted(player);
        };
    }

    private double calculateGeneralPerformance(Player player) {
        return 0.25 * player.getGoals()
             + 0.15 * player.getAssists()
             + 0.10 * player.getShots()
             + 0.10 * player.getKeyPasses()
             + 0.10 * player.getDribbles()
             + 0.10 * player.getTackles()
             + 0.20 * player.getRating();
    }

    private double calculatePositionWeighted(Player player) {
        return switch (player.getPlayerPosition()) {
            case FORWARD   -> 0.35 * player.getGoals()
                            + 0.20 * player.getShots()
                            + 0.15 * player.getAssists()
                            + 0.15 * player.getDribbles()
                            + 0.15 * player.getRating();
            case MIDFIELDER -> 0.20 * player.getKeyPasses()
                            + 0.20 * player.getAssists()
                            + 0.20 * player.getDribbles()
                            + 0.15 * player.getGoals()
                            + 0.10 * player.getTackles()
                            + 0.15 * player.getRating();
            case DEFENDER  -> 0.30 * player.getTackles()
                            + 0.30 * player.getInterceptions()
                            + 0.20 * player.getRating()
                            + 0.10 * player.getGoals()
                            + 0.10 * player.getDribbles();
            case GOALKEEPER -> 0.60 * player.getRating()
                            + 0.20 * player.getTackles()
                            + 0.20 * player.getInterceptions();
        };
    }
}
