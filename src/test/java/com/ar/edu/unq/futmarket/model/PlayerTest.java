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
        player = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .build();
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
    void constructor_defaultTokenPriceIsOne() {
        assertEquals(0, BigDecimal.ONE.compareTo(player.getCurrentTokenPrice()));
    }
}
