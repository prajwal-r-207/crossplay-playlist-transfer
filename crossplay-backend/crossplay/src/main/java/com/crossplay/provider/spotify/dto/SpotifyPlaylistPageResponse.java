package com.crossplay.provider.spotify.dto;

import java.util.List;

public class SpotifyPlaylistPageResponse {
    private List<SpotifyPlaylistItem> items;

    public List<SpotifyPlaylistItem> getItems() {
        return items;
    }

    public void setItems(List<SpotifyPlaylistItem> items) {
        this.items = items;
    }
}
