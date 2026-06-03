package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.exception.*;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioTest {

    private User user;
    private Portfolio portfolio;
    private Player player;
    private User superuser;

    @BeforeEach
    void setUp() {
        superuser = User.builder()
                .username("admin")
                .balance(new BigDecimal("10000.00"))
                .superuser(true)
                .build();
        user = User.builder()
                .username("leandro")
                .balance(new BigDecimal("1000.00"))
                .superuser(false)
                .build();
        portfolio = user.getPortfolio();
        player = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .build();
        player.setCurrentTokenPrice(new BigDecimal("10.00"));
        giveSuperuserPosition(player, 100);
    }

    private void giveSuperuserPosition(Player p, int tokens) {
        Position pos = new Position();
        pos.setPortfolio(superuser.getPortfolio());
        pos.setPlayer(p);
        pos.setTokensAcquired(tokens);
        superuser.getPortfolio().getPositions().add(pos);
    }

    @Test
    void registerPurchase_createsPositionForPlayer() {
        portfolio.registerPurchase(player, 5, superuser);
        Optional<Position> position = portfolio.getPosition(player);
        assertTrue(position.isPresent());
        assertEquals(5, position.get().getTokensAcquired());
    }

    @Test
    void registerPurchase_deductsBalanceFromUser() {
        portfolio.registerPurchase(player, 5, superuser); // 5 * 10 = 50
        assertEquals(0, new BigDecimal("950.00").compareTo(user.getBalance()));
    }

    @Test
    void registerPurchase_secondPurchaseSamePlayer_updatesExistingPosition() {
        portfolio.registerPurchase(player, 3, superuser);
        portfolio.registerPurchase(player, 2, superuser);
        assertEquals(1, portfolio.getPositions().size());
        assertEquals(5, portfolio.getPosition(player).get().getTokensAcquired());
    }

    @Test
    void registerPurchase_multiplePlayers_createsOnePositionEach() {
        Player other = Player.builder()
                .name("Ronaldo")
                .team("Al Nassr")
                .league("Saudi Pro League")
                .playerPosition(PlayerPosition.FORWARD)
                .build();
        other.setCurrentTokenPrice(new BigDecimal("8.00"));
        giveSuperuserPosition(other, 100);

        portfolio.registerPurchase(player, 3, superuser);
        portfolio.registerPurchase(other, 2, superuser);

        assertEquals(2, portfolio.getPositions().size());
    }

    @Test
    void registerPurchase_nullPlayer_throws() {
        assertThrows(PlayerNotFoundException.class,
                () -> portfolio.registerPurchase(null, 5, superuser));
    }

    @Test
    void registerPurchase_zeroQuantity_throws() {
        assertThrows(InvalidTokenQuantityException.class,
                () -> portfolio.registerPurchase(player, 0, superuser));
    }

    @Test
    void registerPurchase_negativeQuantity_throws() {
        assertThrows(InvalidTokenQuantityException.class,
                () -> portfolio.registerPurchase(player, -1, superuser));
    }

    @Test
    void registerPurchase_notEnoughBalance_throws() {
        player.setCurrentTokenPrice(new BigDecimal("300.00")); // 4 * 300 = 1200 > 1000
        assertThrows(InvalidBalanceException.class,
                () -> portfolio.registerPurchase(player, 4, superuser));
    }

    @Test
    void registerPurchase_zeroTokenPrice_throws() {
        player.setCurrentTokenPrice(BigDecimal.ZERO);
        assertThrows(InvalidTokenPriceException.class,
                () -> portfolio.registerPurchase(player, 5, superuser));
    }

    // --- registerSell: happy path ---

    @Test
    void registerSell_decreasesPositionTokens() {
        portfolio.registerPurchase(player, 5, superuser);
        portfolio.registerSell(player, 3, superuser);
        assertEquals(2, portfolio.getPosition(player).get().getTokensAcquired());
    }

    @Test
    void registerSell_creditsUserBalance() {
        portfolio.registerPurchase(player, 5, superuser); // balance: 1000 - 50 = 950
        portfolio.registerSell(player, 3, superuser);     // balance: 950 + 30 = 980
        assertEquals(0, new BigDecimal("980.00").compareTo(user.getBalance()));
    }

    @Test
    void registerSell_allTokens_removesPositionFromPortfolio() {
        portfolio.registerPurchase(player, 5, superuser);
        portfolio.registerSell(player, 5, superuser);
        assertFalse(portfolio.getPosition(player).isPresent());
        assertTrue(portfolio.getPositions().isEmpty());
    }

    @Test
    void registerSell_noExistingPosition_throws() {
        assertThrows(PositionNotFoundException.class,
                () -> portfolio.registerSell(player, 3, superuser));
    }

    @Test
    void registerSell_nullPlayer_throws() {
        assertThrows(PlayerNotFoundException.class,
                () -> portfolio.registerSell(null, 3, superuser));
    }

    @Test
    void registerSell_zeroQuantity_throws() {
        portfolio.registerPurchase(player, 5, superuser);
        assertThrows(InvalidTokenQuantityException.class,
                () -> portfolio.registerSell(player, 0, superuser));
    }

    @Test
    void registerSell_negativeQuantity_throws() {
        portfolio.registerPurchase(player, 5, superuser);
        assertThrows(InvalidTokenQuantityException.class,
                () -> portfolio.registerSell(player, -2, superuser));
    }

    // --- getCurrentValue ---

    @Test
    void getCurrentValue_emptyPortfolio_returnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getCurrentValue()));
    }

    @Test
    void getCurrentValue_withOnePosition_returnsTokensTimesPrice() {
        portfolio.registerPurchase(player, 10, superuser); // 10 * 10 = 100
        assertEquals(0, new BigDecimal("100.00").compareTo(portfolio.getCurrentValue()));
    }

    @Test
    void getCurrentValue_withMultiplePositions_returnsSum() {
        Player other = Player.builder()
                .name("Ronaldo")
                .team("Al Nassr")
                .league("Saudi Pro League")
                .playerPosition(PlayerPosition.FORWARD)
                .build();
        other.setCurrentTokenPrice(new BigDecimal("5.00"));
        giveSuperuserPosition(other, 100);

        portfolio.registerPurchase(player, 10, superuser); // 10 * 10 = 100
        portfolio.registerPurchase(other, 4, superuser);   //  4 *  5 =  20 → total: 120
        assertEquals(0, new BigDecimal("120.00").compareTo(portfolio.getCurrentValue()));
    }

    // --- getProfitLoss ---

    @Test
    void getProfitLoss_emptyPortfolio_returnsZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getProfitLoss()));
    }

    @Test
    void getProfitLoss_priceUnchanged_returnsZero() {
        portfolio.registerPurchase(player, 10, superuser); // bought at 10, price still 10
        assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getProfitLoss()));
    }

    @Test
    void getProfitLoss_priceIncreased_returnsPositiveValue() {
        portfolio.registerPurchase(player, 10, superuser); // bought at 10
        player.setCurrentTokenPrice(new BigDecimal("15.00")); // profit: (15-10)*10 = 50
        assertEquals(0, new BigDecimal("50.00").compareTo(portfolio.getProfitLoss()));
    }

    // --- getPosition ---

    @Test
    void getPosition_playerWithPosition_returnsIt() {
        portfolio.registerPurchase(player, 5, superuser);
        assertTrue(portfolio.getPosition(player).isPresent());
    }

    @Test
    void getPosition_playerWithoutPosition_returnsEmpty() {
        assertTrue(portfolio.getPosition(player).isEmpty());
    }
}
