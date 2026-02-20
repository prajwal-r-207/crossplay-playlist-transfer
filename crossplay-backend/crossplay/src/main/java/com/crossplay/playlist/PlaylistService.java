package com.crossplay.playlist;

import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.MusicPlatformFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MusicPlatformFactory platformFactory;

    public PlaylistService(OAuth2AuthorizedClientService authorizedClientService,
                           MusicPlatformFactory platformFactory) {
        this.authorizedClientService = authorizedClientService;
        this.platformFactory = platformFactory;
    }

    public List<PlaylistDto> getPlaylists(PlatformType platform,
                                          OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        MusicPlatformClient clientProvider =
                platformFactory.getClient(platform);

        return clientProvider.getPlaylists(accessToken);
    }

    public List<TrackDto> getPlaylistTracks(PlatformType platform, String playlistId,
                                            OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        MusicPlatformClient clientProvider =
                platformFactory.getClient(platform);

        return clientProvider.getPlaylistTracks(playlistId, accessToken);
    }

    public PlaylistDto createPlaylist(PlatformType platform,
                                      String name,
                                      String description,
                                      boolean isPublic,
                                      OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        MusicPlatformClient clientProvider =
                platformFactory.getClient(platform);
        return clientProvider.createPlaylist(
                name,
                description,
                isPublic,
                accessToken
        );
    }
}

