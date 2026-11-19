package com.hiretrack.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Change role request DTO.
 * 
 * Admin-only: PUT /api/v1/memberships/{id}/role
 * 
 * Pattern validation: only valid role enum values.
 */
public record ChangeRoleRequest(
    @NotBlank(message = "Role is required")
    @Pattern(
        regexp = "^(ADMIN|RECRUITER|HIRING_MANAGER|INTERVIEWER)$",
        message = "Role must be ADMIN, RECRUITER, HIRING_MANAGER, or INTERVIEWER"
    )
    String role
) {}
