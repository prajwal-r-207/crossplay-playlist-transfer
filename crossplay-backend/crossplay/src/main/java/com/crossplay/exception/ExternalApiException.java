package com.crossplay.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an external platform API (Spotify, YouTube) returns a non-2xx
 * response.
 * Captures enough context to produce a useful log message and an appropriate
 * HTTP response.
 */
public class ExternalApiException extends CrossplayException {

    private final String platform;
    private final int upstreamStatus;
    private final String responseBody;

    public ExternalApiException(String platform, int upstreamStatus, String responseBody) {
        super(
                String.format("%s API returned %d: %s", platform, upstreamStatus, responseBody),
                deriveStatus(upstreamStatus));
        this.platform = platform;
        this.upstreamStatus = upstreamStatus;
        this.responseBody = responseBody;
    }

    /** Map upstream status codes to sensible HTTP responses. */
    private static HttpStatus deriveStatus(int upstreamStatus) {
        return switch (upstreamStatus) {
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }

    public String getPlatform() {
        return platform;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
