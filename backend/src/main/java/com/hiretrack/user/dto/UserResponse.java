package com.hiretrack.user.dto;

import java.util.UUID;

/**
 * User response DTO (for /api/v1/me endpoint).
 * 
 * Returns basic user info (no password hash, no memberships).
 */
public record UserResponse(
    UUID id,
    String email,
    String fullName
) {}
