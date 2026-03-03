package com.crossplay.exception;

import com.crossplay.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central exception handler – translates all exceptions into a consistent
 * {@link ErrorResponse} JSON body with the appropriate HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── TokenNotFoundException → 401 ─────────────────────────────────────────
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFound(
            TokenNotFoundException ex, HttpServletRequest request) {

        log.warn("Token not found for request [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(ex.getStatus(), ex, request);
    }

    // ── ExternalApiException → forwarded / 502 ───────────────────────────────
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(
            ExternalApiException ex, HttpServletRequest request) {

        log.error(
                "External API error [platform={} upstreamStatus={} path={}]: {}",
                ex.getPlatform(), ex.getUpstreamStatus(), request.getRequestURI(), ex.getMessage());
        return build(ex.getStatus(), ex, request);
    }

    // ── PlatformUnsupportedException → 501 ──────────────────────────────────
    @ExceptionHandler(PlatformUnsupportedException.class)
    public ResponseEntity<ErrorResponse> handlePlatformUnsupported(
            PlatformUnsupportedException ex, HttpServletRequest request) {

        log.warn("Unsupported platform request [{}]: {}", request.getRequestURI(), ex.getMessage());
        return build(ex.getStatus(), ex, request);
    }

    // ── CrossplayException (base, catch-all for subclasses) → dynamic ────────
    @ExceptionHandler(CrossplayException.class)
    public ResponseEntity<ErrorResponse> handleCrossplayException(
            CrossplayException ex, HttpServletRequest request) {

        log.error("Crossplay error [path={}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(ex.getStatus(), ex, request);
    }

    // ── Catch-all → 500 ──────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error [path={}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request);
    }

    // ── Builder helpers ───────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex, HttpServletRequest request) {
        return build(status, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
