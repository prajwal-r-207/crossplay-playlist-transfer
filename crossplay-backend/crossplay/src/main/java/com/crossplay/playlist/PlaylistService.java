package com.crossplay.playlist;

import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.provider.MusicPlatformFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {

    private final MusicPlatformFactory factory;

    public PlaylistService(MusicPlatformFactory factory){
        this.factory = factory;
    }

    public List<PlaylistDto> getPlaylists(PlatformType platform) {

        // Mock data for now
        return factory.getClient(platform).getPlaylists();
    }
}
