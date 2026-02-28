package com.crossplay.provider;

import com.crossplay.common.PlatformType;
import com.crossplay.provider.spotify.SpotifyClient;
import com.crossplay.provider.youtube.YouTubeClient;
import org.springframework.stereotype.Component;

@Component
public class MusicPlatformFactory {

    private final SpotifyClient spotifyClient;
    private final YouTubeClient youtubeClient;

    public MusicPlatformFactory(SpotifyClient spotifyClient, YouTubeClient youtubeClient) {
        this.spotifyClient = spotifyClient;
        this.youtubeClient = youtubeClient;
    }

    public MusicPlatformClient getClient(PlatformType platform) {
        return switch (platform) {
            case SPOTIFY -> spotifyClient;
            case YOUTUBE -> youtubeClient;
            case APPLE ->  throw new UnsupportedOperationException("Not implemented yet");
        };
    }
}
