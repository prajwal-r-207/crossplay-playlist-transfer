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

    public void addTracks(PlatformType platform,
                          String playlistId,
                          List<String> trackIds,
                          OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        MusicPlatformClient provider =
                platformFactory.getClient(platform);

        provider.addTracks(playlistId, trackIds, accessToken);
    }

    public PlaylistDto copyPlaylist(
            PlatformType platform,
            String sourcePlaylistId,
            String newName,
            OAuth2AuthenticationToken authentication
    ) {

        //Get access token
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        String accessToken = client.getAccessToken().getTokenValue();

        //Get provider
        MusicPlatformClient provider = platformFactory.getClient(platform);

        //Fetch tracks from source playlist
        List<TrackDto> tracks =
                provider.getPlaylistTracks(sourcePlaylistId, accessToken);

        //Create new playlist
        PlaylistDto newPlaylist =
                provider.createPlaylist(
                        newName,
                        "Copied via CrossPlay",
                        true,
                        accessToken
                );

        //Extract track IDs
        List<String> trackIds =
                tracks.stream()
                        .map(TrackDto::getId)
                        .toList();

        //Spotify allows max 100 tracks per request
        for (int i = 0; i < trackIds.size(); i += 100) {
            List<String> batch =
                    trackIds.subList(i, Math.min(i + 100, trackIds.size()));

            provider.addTracks(newPlaylist.getId(), batch, accessToken);
        }

        return newPlaylist;
    }
}

