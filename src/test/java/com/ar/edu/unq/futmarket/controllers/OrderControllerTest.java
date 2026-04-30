package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.services.impl.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OrderControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderServiceImpl orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Order buildOrderMock(OrderType type) {
        Order order = mock(Order.class);
        when(order.getType()).thenReturn(type);
        when(order.getStatus()).thenReturn(OrderStatus.COMPLETED);
        when(order.getTokenQuantity()).thenReturn(5);
        when(order.getPricePerToken()).thenReturn(BigDecimal.TEN);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("50.00"));
        return order;
    }

    @Test
    @WithMockUser
    void buy_authenticatedUser_returns201WithOrderDTO() throws Exception {
        Order buyOrder = buildOrderMock(OrderType.BUY);
        when(orderService.buy(any(), eq(1L), eq(5))).thenReturn(buyOrder);

        mockMvc.perform(post("/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("playerId", 1, "quantity", 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BUY"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.tokenQuantity").value(5));
    }

    @Test
    @WithMockUser
    void sell_authenticatedUser_returns201WithOrderDTO() throws Exception {
        Order sellOrder = buildOrderMock(OrderType.SELL);
        when(orderService.sell(any(), eq(1L), eq(3))).thenReturn(sellOrder);

        mockMvc.perform(post("/orders/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("playerId", 1, "quantity", 3))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SELL"));
    }

    @Test
    void buy_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("playerId", 1, "quantity", 5))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrdersByUser_returns200WithList() throws Exception {
        Order buyOrder = buildOrderMock(OrderType.BUY);
        when(orderService.getTransactionsByUserId(1L)).thenReturn(List.of(buyOrder));

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("BUY"));
    }

    @Test
    void getOrdersByUser_noOrders_returnsEmptyList() throws Exception {
        when(orderService.getTransactionsByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void buy_missingRequestBody_returns400() throws Exception {
        mockMvc.perform(post("/orders/buy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
