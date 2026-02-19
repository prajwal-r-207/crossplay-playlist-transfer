package com.crossplay.provider.spotify.dto;

import java.util.List;

public class SpotifyPlaylistTracksResponse {

    private List<SpotifyPlaylistTrackItem> items;
    private int total;

    public List<SpotifyPlaylistTrackItem> getItems() { return items; }
    public void setItems(List<SpotifyPlaylistTrackItem> items) { this.items = items; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}