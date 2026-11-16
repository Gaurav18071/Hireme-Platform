package com.hiretrack.health.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PingServiceTest {
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private PingService pingService;
    
    @Test
    void getCurrentTimestamp_returnsFixedTime() {
        Instant fixedInstant = Instant.parse("2026-11-12T00:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        
        Instant result = pingService.getCurrentTimestamp();
        
        assertThat(result).isEqualTo(fixedInstant);
    }
    
    @Test
    void getMessage_returnsPong() {
        String result = pingService.getMessage();
        
        assertThat(result).isEqualTo("pong");
    }
}
