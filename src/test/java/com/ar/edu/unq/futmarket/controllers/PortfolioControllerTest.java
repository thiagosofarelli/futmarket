package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.PortfolioNotFoundException;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.services.impl.PortfolioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PortfolioControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private PortfolioServiceImpl portfolioService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getPortfolioByUserId_found_returns200() throws Exception {
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getPositions()).thenReturn(List.of());
        when(portfolio.getCurrentValue()).thenReturn(BigDecimal.ZERO);
        when(portfolio.getProfitLoss()).thenReturn(BigDecimal.ZERO);
        when(portfolioService.findByUserId(1L)).thenReturn(portfolio);

        mockMvc.perform(get("/portfolios/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positions").isArray());
    }

    @Test
    void getPortfolioByUserId_notFound_returns404() throws Exception {
        when(portfolioService.findByUserId(99L)).thenThrow(new PortfolioNotFoundException());

        mockMvc.perform(get("/portfolios/user/99"))
                .andExpect(status().isNotFound());
    }
}
