package com.crossplay.exception.dto;

import java.time.LocalDateTime;

/**
 * Standard JSON error response body returned by
 * {@link com.crossplay.exception.GlobalExceptionHandler}.
 */
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }
}
