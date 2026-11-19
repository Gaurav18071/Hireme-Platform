package com.hiretrack.common.exception;

/**
 * NotFoundException: resource doesn't exist or belongs to another organization (404).
 * 
 * CRITICAL for tenant isolation:
 * - Return 404 (not 403) when resource exists but belongs to another org
 * - This prevents leaking existence of other organizations' data
 * 
 * Used for:
 * - Entity not found by ID
 * - Entity found but wrong organization_id (IDOR protection)
 * 
 * GlobalExceptionHandler maps this to HTTP 404 Not Found.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
