package com.crossplay.playlist;

import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.PlaylistDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public List<PlaylistDto> getPLaylists(@RequestParam PlatformType platform) {
        return playlistService.getPlaylists(platform);
    }
}
