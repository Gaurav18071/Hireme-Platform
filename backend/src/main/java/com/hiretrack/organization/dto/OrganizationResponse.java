package com.hiretrack.organization.dto;

import java.util.UUID;

/**
 * Organization response DTO (for /api/v1/organizations/current).
 * 
 * Includes the current user's role in this organization.
 */
public record OrganizationResponse(
    UUID id,
    String name,
    String role
) {}
