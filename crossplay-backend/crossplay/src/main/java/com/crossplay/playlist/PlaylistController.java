package com.crossplay.playlist;

import com.crossplay.auth.DevTokenStore;
import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.AddTracksRequest;
import com.crossplay.playlist.dto.CreatePlaylistRequest;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.MusicPlatformFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MusicPlatformFactory platformFactory;
    private final DevTokenStore tokenStore;

    public PlaylistController(PlaylistService playlistService, OAuth2AuthorizedClientService authorizedClientService,
                              MusicPlatformFactory platformFactory, DevTokenStore tokenStore) {
        this.playlistService = playlistService;
        this.authorizedClientService = authorizedClientService;
        this.platformFactory = platformFactory;
        this.tokenStore = tokenStore;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlaylistDto> getPlaylists(
            @RequestParam PlatformType platform,
            OAuth2AuthenticationToken authentication
    ) {
        return playlistService.getPlaylists(platform, authentication);
    }

    @GetMapping(value = "/{playlistId}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TrackDto> getItems(
            @PathVariable String playlistId,
            @RequestParam PlatformType platform,
            OAuth2AuthenticationToken authentication
    ) {
        return playlistService.getPlaylistTracks(platform, playlistId, authentication);
    }

    @PostMapping
    public PlaylistDto createPlaylist(
            @RequestParam PlatformType platform,
            @RequestBody CreatePlaylistRequest request,
            OAuth2AuthenticationToken authentication
    ) {
        return playlistService.createPlaylist(
                platform,
                request.getName(),
                request.getDescription(),
                request.isPublic(),
                authentication
        );
    }

    @PostMapping("/{playlistId}/items")
    public void addTracks(
            @PathVariable String playlistId,
            @RequestParam PlatformType platform,
            @RequestBody AddTracksRequest request,
            OAuth2AuthenticationToken authentication
    ) {
        playlistService.addTracks(
                platform,
                playlistId,
                request.getTrackIds(),
                authentication
        );
    }

    @PostMapping("/{playlistId}/copy")
    public PlaylistDto copyPlaylist(
            @PathVariable String playlistId,
            @RequestParam PlatformType platform,
            @RequestParam String newName,
            OAuth2AuthenticationToken authentication
    ) {
        return playlistService.copyPlaylist(
                platform,
                playlistId,
                newName,
                authentication
        );
    }

    @GetMapping("/search")
    public List<TrackDto> search(
            @RequestParam PlatformType platform,
            @RequestParam String query,
            OAuth2AuthenticationToken authentication
    ) {

//        OAuth2AuthorizedClient client =
//                authorizedClientService.loadAuthorizedClient(
//                        platform.getRegistrationId(),
//                        authentication.getName()
//                );
//
//        String accessToken = client.getAccessToken().getTokenValue();
        String accessToken = tokenStore.getToken(platform.getRegistrationId());
        MusicPlatformClient provider = platformFactory.getClient(platform);

        return provider.searchTracks(query, accessToken);
    }
}
