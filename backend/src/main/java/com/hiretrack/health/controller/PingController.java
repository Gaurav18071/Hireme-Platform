package com.hiretrack.health.controller;

import com.hiretrack.health.dto.PingResponse;
import com.hiretrack.health.service.PingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {
    
    private final PingService pingService;
    
    public PingController(PingService pingService) {
        this.pingService = pingService;
    }
    
    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse(
            pingService.getMessage(),
            pingService.getCurrentTimestamp(),
            "1.0.0"
        );
    }
}
