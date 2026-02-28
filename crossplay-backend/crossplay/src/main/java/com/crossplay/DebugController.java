package com.crossplay;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public DebugController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/api/debug/token")
    public String getAccessToken(OAuth2AuthenticationToken authentication) {

        if (authentication == null) {
            return "User not authenticated";
        }

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        if (client == null) {
            return "No authorized client found";
        }

        return client.getAccessToken().getTokenValue();
    }
}
