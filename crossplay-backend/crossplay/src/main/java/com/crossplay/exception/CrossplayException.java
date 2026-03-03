package com.crossplay.exception;

import org.springframework.http.HttpStatus;

/**
 * Base runtime exception for all Crossplay-specific errors.
 * Carries an HTTP status so the global handler can derive the response code.
 */
public class CrossplayException extends RuntimeException {

    private final HttpStatus status;

    public CrossplayException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CrossplayException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
