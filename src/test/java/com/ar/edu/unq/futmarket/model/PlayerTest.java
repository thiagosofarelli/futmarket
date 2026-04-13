package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Messi", "Inter Miami", "MLS", PlayerPosition.FORWARD);
    }

    // --- Constructor ---

    @Test
    void constructor_setsFieldsCorrectly() {
        assertEquals("Messi", player.getName());
        assertEquals("Inter Miami", player.getTeam());
        assertEquals("MLS", player.getLeague());
        assertEquals(PlayerPosition.FORWARD, player.getPlayerPosition());
    }

    @Test
    void constructor_defaultAvailableTokensIs100() {
        assertEquals(100, player.getAvailableTokens());
    }

    @Test
    void constructor_defaultTokenPriceIsOne() {
        assertEquals(0, BigDecimal.ONE.compareTo(player.getCurrentTokenPrice()));
    }

    // --- subAvailableTokens ---

    @Test
    void subAvailableTokens_decreasesCorrectly() {
        player.subAvailableTokens(10);
        assertEquals(90, player.getAvailableTokens());
    }

    @Test
    void subAvailableTokens_allTokens_leavesZero() {
        player.subAvailableTokens(100);
        assertEquals(0, player.getAvailableTokens());
    }

    @Test
    void subAvailableTokens_withZeroQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () -> player.subAvailableTokens(0));
    }

    @Test
    void subAvailableTokens_withNegativeQuantity_throws() {
        assertThrows(IllegalArgumentException.class, () -> player.subAvailableTokens(-5));
    }

    @Test
    void subAvailableTokens_moreThanAvailable_throws() {
        assertThrows(IllegalArgumentException.class, () -> player.subAvailableTokens(101));
    }

    // --- addAvailableTokens ---

    @Test
    void addAvailableTokens_increasesCorrectly() {
        player.subAvailableTokens(20);
        player.addAvailableTokens(10);
        assertEquals(90, player.getAvailableTokens());
    }

    @Test
    void addAvailableTokens_restoresAllTokens() {
        player.subAvailableTokens(100);
        player.addAvailableTokens(100);
        assertEquals(100, player.getAvailableTokens());
    }

    @Test
    void addAvailableTokens_withZeroQuantity_throws() {
        player.subAvailableTokens(10);
        assertThrows(IllegalArgumentException.class, () -> player.addAvailableTokens(0));
    }

    @Test
    void addAvailableTokens_withNegativeQuantity_throws() {
        player.subAvailableTokens(10);
        assertThrows(IllegalArgumentException.class, () -> player.addAvailableTokens(-1));
    }

    @Test
    void addAvailableTokens_exceedsIssuedTokens_throws() {
        // availableTokens already at max (100), can't add more
        assertThrows(IllegalArgumentException.class, () -> player.addAvailableTokens(1));
    }

    @Test
    void addAvailableTokens_wouldExceedIssuedTokens_throws() {
        player.subAvailableTokens(5);
        // availableTokens = 95, adding 10 would exceed 100
        assertThrows(IllegalArgumentException.class, () -> player.addAvailableTokens(10));
    }
}
