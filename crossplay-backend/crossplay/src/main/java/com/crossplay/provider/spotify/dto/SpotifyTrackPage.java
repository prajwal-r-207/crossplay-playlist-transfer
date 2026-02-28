package com.crossplay.provider.spotify.dto;

import java.util.List;

public class SpotifyTrackPage {
    private List<SpotifyTrack> items;

    public List<SpotifyTrack> getItems() {
        return items;
    }

    public void setItems(List<SpotifyTrack> items) {
        this.items = items;
    }
}
