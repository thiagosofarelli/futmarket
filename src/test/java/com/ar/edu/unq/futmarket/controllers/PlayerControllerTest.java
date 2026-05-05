package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.services.impl.PlayerServiceImpl;
import com.ar.edu.unq.futmarket.services.impl.QuoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PlayerControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PlayerServiceImpl playerService;

    @MockitoBean
    private QuoteServiceImpl quoteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Player buildPlayer(String name) {
        return Player.builder()
                .name(name)
                .team("Team A")
                .league(League.PL.getFullName())
                .playerPosition(PlayerPosition.FORWARD)
                .currentTokenPrice(BigDecimal.TEN)
                .build();
    }

    @Test
    void getPlayers_noFilters_returns200WithPage() throws Exception {
        when(playerService.findByFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildPlayer("Haaland"))));

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Haaland"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getPlayers_withFilters_callsServiceWithCorrectFilters() throws Exception {
        when(playerService.findByFilters(eq(League.PL), isNull(), eq(PlayerPosition.FORWARD), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(buildPlayer("Haaland"))));

        mockMvc.perform(get("/players")
                        .param("league", "PL")
                        .param("playerPosition", "FORWARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(playerService).findByFilters(eq(League.PL), isNull(), eq(PlayerPosition.FORWARD), any(Pageable.class));
    }

    @Test
    void getRanking_returns200WithList() throws Exception {
        when(playerService.getRanking()).thenReturn(List.of(
                buildPlayer("Haaland"),
                buildPlayer("Saliba")));

        mockMvc.perform(get("/players/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Haaland"));
    }

    @Test
    void getPlayer_existingId_returns200() throws Exception {
        when(playerService.findById(1L)).thenReturn(buildPlayer("Courtois"));

        mockMvc.perform(get("/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Courtois"));
    }

    @Test
    void getPlayer_notFound_returns404() throws Exception {
        when(playerService.findById(99L)).thenThrow(new PlayerNotFoundException());

        mockMvc.perform(get("/players/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlayerQuotes_returns200WithList() throws Exception {
        when(playerService.findById(1L)).thenReturn(buildPlayer("Haaland"));
        when(quoteService.findByPlayerId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/players/1/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPlayerQuotes_playerNotFound_returns404() throws Exception {
        when(playerService.findById(99L)).thenThrow(new PlayerNotFoundException());

        mockMvc.perform(get("/players/99/quotes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void syncPlayers_returns200() throws Exception {
        mockMvc.perform(post("/players/sync"))
                .andExpect(status().isOk());

        verify(playerService).syncPlayers();
    }

    @Test
    void syncStats_returns202() throws Exception {
        mockMvc.perform(post("/players/stats/sync"))
                .andExpect(status().isAccepted());

        verify(playerService).syncPlayersStatsForAllLeagues();
    }

    @Test
    void syncStatsByLeague_returns202() throws Exception {
        mockMvc.perform(post("/players/stats/sync/PL"))
                .andExpect(status().isAccepted());

        verify(playerService).syncPlayersStatsByLeague(League.PL);
    }
}
