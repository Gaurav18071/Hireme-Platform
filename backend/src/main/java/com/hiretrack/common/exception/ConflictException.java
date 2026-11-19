package com.hiretrack.common.exception;

/**
 * ConflictException: request conflicts with current state (409).
 * 
 * Used for:
 * - Duplicate email on registration
 * - Business rule violations (e.g., "Email already registered")
 * 
 * GlobalExceptionHandler maps this to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
