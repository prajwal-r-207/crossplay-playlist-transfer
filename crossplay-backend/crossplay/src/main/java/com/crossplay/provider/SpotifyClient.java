package com.crossplay.provider;

import com.crossplay.playlist.dto.PlaylistDto;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class SpotifyClient implements MusicPlatformClient {

    @Override
    public List<PlaylistDto> getPlaylists() {
        return List.of(
                new PlaylistDto("1", "Spotify workout", 25),
                new PlaylistDto("2", "Spotify chill", 34)
        );
    }
}
