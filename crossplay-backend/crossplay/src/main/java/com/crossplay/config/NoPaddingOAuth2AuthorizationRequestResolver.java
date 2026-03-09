package com.crossplay.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Custom resolver that replaces the default Base64-with-padding state (and PKCE
 * code verifier/challenge) with URL-safe, no-padding equivalents.
 *
 * <p>
 * Spotify double-encodes {@code =} padding characters in the {@code state}
 * callback parameter, turning {@code %3D} → {@code %253D}, which breaks Spring
 * Security's state comparison. Removing padding entirely avoids the problem.
 * </p>
 *
 * <p>
 * <strong>PKCE note:</strong> {@code code_verifier} is stored in the request
 * <em>attributes</em> (server-side) so Spring Security can send it during the
 * token exchange. Only {@code code_challenge} and {@code code_challenge_method}
 * are added to the authorization URL as query parameters.
 * </p>
 */
public class NoPaddingOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;
    private final SecureRandom secureRandom = new SecureRandom();

    public NoPaddingOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository repo, String baseAuthorizationUri) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repo, baseAuthorizationUri);

        this.delegate.setAuthorizationRequestCustomizer(builder -> {
            // 1. Always replace state with a no-padding equivalent
            builder.state(generateNoPaddingState());

            // 2. If this is a PKCE flow, regenerate code_verifier / code_challenge
            // without padding. We use a single-element array as a "holder" so that
            // the verifier generated inside additionalParameters() can be forwarded
            // to attributes() (both are Consumer lambdas with no return value).
            String[] verifierHolder = new String[1];

            builder.additionalParameters(params -> {
                if (!params.containsKey("code_challenge")) {
                    return; // Not a PKCE flow — leave params untouched
                }
                // Generate a fresh no-padding code verifier
                byte[] bytes = new byte[32];
                secureRandom.nextBytes(bytes);
                String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                verifierHolder[0] = codeVerifier;

                // Only the challenge goes into the URL
                params.put("code_challenge", generateS256Challenge(codeVerifier));
                params.put("code_challenge_method", "S256");

                // Make absolutely sure code_verifier is NOT in the URL params
                params.remove(PkceParameterNames.CODE_VERIFIER);
            });

            builder.attributes(attrs -> {
                if (verifierHolder[0] != null) {
                    // Store the verifier server-side so Spring Security sends it
                    // during the token exchange (not exposed in the redirect URL)
                    attrs.put(PkceParameterNames.CODE_VERIFIER, verifierHolder[0]);
                }
            });
        });
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return delegate.resolve(request, clientRegistrationId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Generates a 32-byte cryptographically random state with no Base64 padding.
     */
    private String generateNoPaddingState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Computes a SHA-256 S256 PKCE code challenge with no Base64 padding. */
    private String generateS256Challenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
