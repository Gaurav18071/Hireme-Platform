package com.hiretrack.membership.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Member response DTO (for organization member listing).
 * 
 * Admin-only endpoint: GET /api/v1/organizations/current/members
 */
public record MemberResponse(
    UUID id,
    String fullName,
    String email,
    String role,
    String status,
    Instant joinedAt
) {}
