package com.crossplay.provider.youtube.dto;

import java.util.List;

public class YouTubePlaylistResponse {
    private List<YouTubePlaylistItem> items;

    public List<YouTubePlaylistItem> getItems() {
        return items;
    }

    public void setItems(List<YouTubePlaylistItem> items) {
        this.items = items;
    }
}
