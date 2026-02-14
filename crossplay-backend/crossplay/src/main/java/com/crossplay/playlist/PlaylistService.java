package com.crossplay.playlist;

import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.PlaylistDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {

    public List<PlaylistDto> getPlaylists(PlatformType platform) {

        // Mock data for now
        return List.of(
                new PlaylistDto("1", "Workout Mix", 25),
                new PlaylistDto("2", "Chill Vibes", 40)
        );
    }
}
