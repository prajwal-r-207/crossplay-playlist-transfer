package com.crossplay.provider.spotify.dto;

public class SpotifySearchResponse {
    private SpotifyTrackPage tracks;

    public SpotifyTrackPage getTracks() {
        return tracks;
    }

    public void setTracks(SpotifyTrackPage tracks) {
        this.tracks = tracks;
    }
}
