package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.exception.*;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    private Player player;
    private Position position;

    @BeforeEach
    void setUp() {
        player = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .currentTokenPrice(new BigDecimal("5.00"))
                .build();
        player.setCurrentTokenPrice(new BigDecimal("5.00"));
        player.setCurrentTokenPrice(new BigDecimal("5.00"));

        position = new Position();
        position.setPlayer(player);
    }


    @Test
    void registerPurchase_setsTokensAndAveragePrice() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        assertEquals(10, position.getTokensAcquired());
        assertEquals(0, new BigDecimal("5.0000").compareTo(position.getAveragePurchasePrice()));
    }

    @Test
    void registerPurchase_secondPurchase_recalculatesWeightedAverage() {
        position.registerPurchase(10, new BigDecimal("4.00")); // cost: 40
        position.registerPurchase(10, new BigDecimal("6.00")); // cost: 60 → avg: 100/20 = 5.00
        assertEquals(20, position.getTokensAcquired());
        assertEquals(0, new BigDecimal("5.0000").compareTo(position.getAveragePurchasePrice()));
    }

    @Test
    void registerPurchase_differentQuantities_recalculatesWeightedAverage() {
        position.registerPurchase(20, new BigDecimal("3.00")); // cost: 60
        position.registerPurchase(10, new BigDecimal("6.00")); // cost: 60 → avg: 120/30 = 4.00
        assertEquals(30, position.getTokensAcquired());
        assertEquals(0, new BigDecimal("4.0000").compareTo(position.getAveragePurchasePrice()));
    }

    @Test
    void registerPurchase_zeroQuantity_throws() {
        BigDecimal price = new BigDecimal("5.00");
        assertThrows(InvalidTokenQuantityException.class,
                () -> position.registerPurchase(0, price));
    }

    @Test
    void registerPurchase_negativeQuantity_throws() {
        BigDecimal price = new BigDecimal("5.00");
        assertThrows(InvalidTokenQuantityException.class,
                () -> position.registerPurchase(-1, price));
    }

    @Test
    void registerPurchase_nullPrice_throws() {
        assertThrows(InvalidPurchasePriceException.class,
                () -> position.registerPurchase(10, null));
    }

    @Test
    void registerPurchase_zeroPricePerToken_throws() {
        assertThrows(InvalidPurchasePriceException.class,
                () -> position.registerPurchase(10, BigDecimal.ZERO));
    }

    @Test
    void registerPurchase_negativePricePerToken_throws() {
        BigDecimal negativePrice = new BigDecimal("-1.00");
        assertThrows(InvalidPurchasePriceException.class,
                () -> position.registerPurchase(10, negativePrice));
    }

    @Test
    void registerPurchase_nullPlayer_throws() {
        position.setPlayer(null);
        BigDecimal price = new BigDecimal("5.00");
        assertThrows(UserNotFoundException.class,
                () -> position.registerPurchase(10, price));
    }

    // --- registerSell ---

    @Test
    void registerSell_decreasesTokensAndRestoresAvailability() {
        position.registerPurchase(10, new BigDecimal("5.00")); // availableTokens: 90
        position.registerSell(4);
        assertEquals(6, position.getTokensAcquired());
    }

    @Test
    void registerSell_allTokens_setsTokensToZero() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        position.registerSell(10);
        assertEquals(0, position.getTokensAcquired());
    }

    @Test
    void registerSell_zeroQuantity_throws() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        assertThrows(InvalidTokenQuantityException.class, () -> position.registerSell(0));
    }

    @Test
    void registerSell_negativeQuantity_throws() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        assertThrows(InvalidTokenQuantityException.class, () -> position.registerSell(-3));
    }

    @Test
    void registerSell_moreThanHeld_throws() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        assertThrows(InsufficientTokensException.class, () -> position.registerSell(11));
    }

    @Test
    void registerSell_nullPlayer_throws() {
        position.setTokensAcquired(5);
        position.setPlayer(null);
        assertThrows(PlayerNotFoundException.class, () -> position.registerSell(3));
    }

    @Test
    void getCurrentValue_returnsTokensTimesCurrentPrice() {
        position.registerPurchase(10, new BigDecimal("5.00")); // player price = 5.00
        assertEquals(0, new BigDecimal("50.00").compareTo(position.getCurrentValue()));
    }

    @Test
    void getCurrentValue_priceChanged_reflectsNewPrice() {
        position.registerPurchase(10, new BigDecimal("5.00"));
        player.setCurrentTokenPrice(new BigDecimal("8.00"));
        assertEquals(0, new BigDecimal("80.00").compareTo(position.getCurrentValue()));
    }

    @Test
    void getCurrentValue_nullPlayer_returnsZero() {
        position.setPlayer(null);
        assertEquals(0, BigDecimal.ZERO.compareTo(position.getCurrentValue()));
    }

    @Test
    void getInvestedAmount_returnsTokensTimesAveragePrice() {
        position.registerPurchase(10, new BigDecimal("4.00"));
        assertEquals(0, new BigDecimal("40.00").compareTo(position.getInvestedAmount()));
    }

    @Test
    void getProfitLoss_zeroProfitWhenPriceUnchanged() {
        position.registerPurchase(10, new BigDecimal("5.00")); // price = 5.00
        assertEquals(0, BigDecimal.ZERO.compareTo(position.getProfitLoss()));
    }

    @Test
    void getProfitLoss_positiveProfitWhenPriceIncreased() {
        position.registerPurchase(10, new BigDecimal("3.00"));
        player.setCurrentTokenPrice(new BigDecimal("5.00"));
        // profit = (5 - 3) * 10 = 20
        assertEquals(0, new BigDecimal("20.00").compareTo(position.getProfitLoss()));
    }

    @Test
    void getProfitLoss_negativeProfitWhenPriceDecreased() {
        position.registerPurchase(10, new BigDecimal("7.00"));
        player.setCurrentTokenPrice(new BigDecimal("5.00"));
        // loss = (5 - 7) * 10 = -20
        assertEquals(0, new BigDecimal("-20.00").compareTo(position.getProfitLoss()));
    }
}
