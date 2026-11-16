package com.hiretrack.health.dto;

import java.time.Instant;

public record PingResponse(
    String message,
    Instant timestamp,
    String version
) {}
