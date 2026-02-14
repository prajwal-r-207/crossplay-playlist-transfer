package com.crossplay.provider;

import com.crossplay.common.PlatformType;
import org.springframework.stereotype.Component;

@Component
public class MusicPlatformFactory {

    private final SpotifyClient spotifyClient;

    public MusicPlatformFactory(SpotifyClient spotifyClient) {
        this.spotifyClient = spotifyClient;
    }

    public MusicPlatformClient getClient(PlatformType platform) {
        return switch (platform) {
            case SPOTIFY -> spotifyClient;
            case YOUTUBE ->  throw new UnsupportedOperationException("Not implemented yet");
            case APPLE ->  throw new UnsupportedOperationException("Not implemented yet");
        };
    }
}
