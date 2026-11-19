package com.hiretrack.auth.dto;

import java.util.UUID;

/**
 * Auth response DTO (returned on register and login).
 * 
 * Contains user info and their current organization context.
 * Phase 2: First active membership is current organization.
 * Phase 3+: User can switch organizations.
 */
public record AuthResponse(
    UUID userId,
    String email,
    String fullName,
    UUID organizationId,
    String organizationName,
    String role
) {}
