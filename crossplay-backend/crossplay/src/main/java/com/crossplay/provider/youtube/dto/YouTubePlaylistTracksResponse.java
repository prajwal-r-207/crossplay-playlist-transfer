package com.crossplay.provider.youtube.dto;

import java.util.List;

public class YouTubePlaylistTracksResponse {
    private List<YouTubePlaylistTrackItemResponse> items;

    public List<YouTubePlaylistTrackItemResponse> getItems() {
        return items;
    }

    public void setItems(List<YouTubePlaylistTrackItemResponse> items) {
        this.items = items;
    }
}
