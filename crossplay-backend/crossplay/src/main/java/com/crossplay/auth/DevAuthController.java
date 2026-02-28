package com.crossplay.auth;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevAuthController {

    private final DevTokenStore tokenStore;

    public DevAuthController(DevTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @GetMapping("/dev/save-token")
    public String saveToken(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client
    ) {

        String registrationId = client.getClientRegistration().getRegistrationId();
        String token = client.getAccessToken().getTokenValue();

        tokenStore.saveToken(registrationId, token);

        return "Saved token"+ token+  " for " + registrationId;
    }
}
