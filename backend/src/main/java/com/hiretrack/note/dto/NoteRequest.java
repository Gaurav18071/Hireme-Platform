package com.hiretrack.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Note request DTO (create and update).
 * TEST RESOURCE (Phase 2 only).
 */
public record NoteRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be 1-255 characters")
    String title,
    
    String content  // Optional
) {}
