package com.hiretrack.health.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class PingService {
    
    private final Clock clock;
    
    public PingService(Clock clock) {
        this.clock = clock;
    }
    
    public Instant getCurrentTimestamp() {
        return Instant.now(clock);
    }
    
    public String getMessage() {
        return "pong";
    }
}
