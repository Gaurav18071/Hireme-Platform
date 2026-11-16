package com.hiretrack.health.controller;

import com.hiretrack.health.service.PingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PingController.class)
class PingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private PingService pingService;
    
    @Test
    void ping_returnsOkWithCorrectJson() throws Exception {
        Instant fixedTime = Instant.parse("2026-11-12T00:00:00Z");
        when(pingService.getMessage()).thenReturn("pong");
        when(pingService.getCurrentTimestamp()).thenReturn(fixedTime);
        
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("pong"))
            .andExpect(jsonPath("$.timestamp").value("2026-11-12T00:00:00Z"))
            .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
