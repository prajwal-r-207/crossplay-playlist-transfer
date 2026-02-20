package com.crossplay.playlist;

import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.CreatePlaylistRequest;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
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
}
