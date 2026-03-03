package com.crossplay.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when no OAuth access token is found for a given platform.
 * Maps to HTTP 401 Unauthorized.
 */
public class TokenNotFoundException extends CrossplayException {

    public TokenNotFoundException(String platform) {
        super("No access token found for platform: " + platform, HttpStatus.UNAUTHORIZED);
    }
}
