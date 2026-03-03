package com.crossplay.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested platform is not yet supported.
 * Maps to HTTP 501 Not Implemented.
 */
public class PlatformUnsupportedException extends CrossplayException {

    public PlatformUnsupportedException(String platform) {
        super("Platform not yet supported: " + platform, HttpStatus.NOT_IMPLEMENTED);
    }
}
