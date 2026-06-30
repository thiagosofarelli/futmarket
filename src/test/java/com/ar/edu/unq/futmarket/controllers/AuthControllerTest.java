package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.InvalidCredentialsException;
import com.ar.edu.unq.futmarket.exception.UsernameAlreadyExistsException;
import com.ar.edu.unq.futmarket.services.impl.AuthServiceImpl;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthServiceImpl authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        when(authService.register(anyString(), anyString())).thenReturn("jwt-token-123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "testuser", "password", "testpass"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        when(authService.register(anyString(), anyString())).thenThrow(new UsernameAlreadyExistsException());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "existing", "password", "pass"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        when(authService.login(anyString(), anyString())).thenReturn("jwt-token-456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "testuser", "password", "testpass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-456"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(anyString(), anyString())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "testuser", "password", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_missingRequestBody_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
