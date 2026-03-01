package com.crossplay.migration;

import com.crossplay.migration.dto.MigrationRequest;
import com.crossplay.migration.dto.MigrationResult;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migration")
public class MigrationController {
    private final MigrationService migrationService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public MigrationController(MigrationService migrationService, OAuth2AuthorizedClientService authorizedClientService) {
        this.migrationService = migrationService;
        this.authorizedClientService = authorizedClientService;
    }

    @PostMapping("/migrate")
    public MigrationResult migrate(
            @RequestBody MigrationRequest request,
            OAuth2AuthenticationToken authentication
    ) {
        return migrationService.migrate(request, authentication);
    }
}
