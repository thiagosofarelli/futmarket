package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.services.BootstrapResult;
import com.ar.edu.unq.futmarket.services.impl.BootstrapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BootstrapControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private BootstrapServiceImpl bootstrapService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void initializeDemoData_returns200WithBootstrapResponse() throws Exception {
        BootstrapResult result = new BootstrapResult(true, 3, 4, 3, 4);
        when(bootstrapService.initializeDemoData()).thenReturn(result);

        mockMvc.perform(post("/admin/bootstrap/demo-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.superuserCreated").value(true))
                .andExpect(jsonPath("$.usersCreated").value(3))
                .andExpect(jsonPath("$.playersCreated").value(4))
                .andExpect(jsonPath("$.ordersCreated").value(3))
                .andExpect(jsonPath("$.quotesCreated").value(4));
    }

    @Test
    void initializeDemoData_callsBootstrapService() throws Exception {
        when(bootstrapService.initializeDemoData()).thenReturn(new BootstrapResult(false, 0, 0, 0, 0));

        mockMvc.perform(post("/admin/bootstrap/demo-data"))
                .andExpect(status().isOk());

        verify(bootstrapService).initializeDemoData();
    }

    @Test
    void removeAllData_returns200WithSuccessMessage() throws Exception {
        doNothing().when(bootstrapService).removeAllData();

        mockMvc.perform(delete("/admin/bootstrap/all-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All data removed"));
    }

    @Test
    void removeAllData_callsBootstrapService() throws Exception {
        doNothing().when(bootstrapService).removeAllData();

        mockMvc.perform(delete("/admin/bootstrap/all-data"))
                .andExpect(status().isOk());

        verify(bootstrapService).removeAllData();
    }

    @Test
    void initializeDemoData_idempotentResult_returns200() throws Exception {
        BootstrapResult nothingCreated = new BootstrapResult(false, 0, 0, 0, 0);
        when(bootstrapService.initializeDemoData()).thenReturn(nothingCreated);

        mockMvc.perform(post("/admin/bootstrap/demo-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.superuserCreated").value(false))
                .andExpect(jsonPath("$.usersCreated").value(0));
    }
}
