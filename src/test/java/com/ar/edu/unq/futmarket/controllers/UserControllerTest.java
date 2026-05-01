package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.PortfolioNotFoundException;
import com.ar.edu.unq.futmarket.exception.UserNotFoundException;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.services.impl.OrderServiceImpl;
import com.ar.edu.unq.futmarket.services.impl.PortfolioServiceImpl;
import com.ar.edu.unq.futmarket.services.impl.UserServiceImpl;
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
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private PortfolioServiceImpl portfolioService;

    @MockitoBean
    private OrderServiceImpl orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getUser_found_returns200WithUserDTO() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getBalance()).thenReturn(new BigDecimal("500.00"));
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.findById(99L)).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPortfolio_found_returns200() throws Exception {
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getPositions()).thenReturn(List.of());
        when(portfolioService.findByUserId(1L)).thenReturn(portfolio);

        mockMvc.perform(get("/users/1/portfolio"))
                .andExpect(status().isOk());
    }

    @Test
    void getPortfolio_notFound_returns404() throws Exception {
        when(portfolioService.findByUserId(99L)).thenThrow(new PortfolioNotFoundException());

        mockMvc.perform(get("/users/99/portfolio"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactions_returns200WithList() throws Exception {
        Order order = mock(Order.class);
        when(order.getType()).thenReturn(OrderType.BUY);
        when(orderService.getTransactionsByUserId(1L)).thenReturn(List.of(order));

        mockMvc.perform(get("/users/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getTransactions_noTransactions_returnsEmptyList() throws Exception {
        when(orderService.getTransactionsByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
