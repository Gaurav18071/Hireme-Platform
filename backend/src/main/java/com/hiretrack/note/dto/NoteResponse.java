package com.hiretrack.note.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Note response DTO.
 * TEST RESOURCE (Phase 2 only).
 */
public record NoteResponse(
    UUID id,
    String title,
    String content,
    Instant createdAt,
    Instant updatedAt
) {}
