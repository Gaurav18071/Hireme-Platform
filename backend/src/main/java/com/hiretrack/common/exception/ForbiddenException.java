package com.hiretrack.common.exception;

/**
 * ForbiddenException: user is authenticated but lacks permission (403).
 * 
 * Used for:
 * - Role checks (e.g., "Only admins can change roles")
 * - Business rules (e.g., "Cannot remove last admin")
 * 
 * GlobalExceptionHandler maps this to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
