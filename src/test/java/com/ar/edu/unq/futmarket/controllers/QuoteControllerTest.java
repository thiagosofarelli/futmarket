package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.services.impl.QuoteServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class QuoteControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuoteServiceImpl quoteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void recalculate_withGeneralPerformance_returns200() throws Exception {
        mockMvc.perform(post("/quotes/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("strategy", "GENERAL_PERFORMANCE"))))
                .andExpect(status().isOk());

        verify(quoteService).recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);
    }

    @Test
    void recalculate_withPositionWeighted_returns200() throws Exception {
        mockMvc.perform(post("/quotes/recalculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("strategy", "POSITION_WEIGHTED"))))
                .andExpect(status().isOk());

        verify(quoteService).recalculateAll(ValuationStrategy.POSITION_WEIGHTED);
    }

    @Test
    void getQuotesByPlayer_returns200WithList() throws Exception {
        Quote quote = mock(Quote.class);
        when(quote.getStrategy()).thenReturn(ValuationStrategy.GENERAL_PERFORMANCE);
        when(quoteService.findByPlayerId(1L)).thenReturn(List.of(quote));

        mockMvc.perform(get("/quotes/player/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].strategy").value("GENERAL_PERFORMANCE"));
    }

    @Test
    void getQuotesByPlayer_noQuotes_returnsEmptyList() throws Exception {
        when(quoteService.findByPlayerId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/quotes/player/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
