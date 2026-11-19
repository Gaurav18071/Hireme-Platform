package com.hiretrack.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request DTO.
 * 
 * Validates:
 * - Email format (@Email annotation)
 * - Password length (10-128 characters, OWASP recommendation)
 * - Full name and organization name (2-255 characters)
 * 
 * Bean Validation (@Valid in controller) automatically returns 400 if constraints fail.
 * 
 * Similar to Zod schema:
 *   z.object({ email: z.string().email(), password: z.string().min(10), ... })
 */
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 10, max = 128, message = "Password must be 10-128 characters")
    String password,
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be 2-255 characters")
    String fullName,
    
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be 2-255 characters")
    String organizationName
) {}
