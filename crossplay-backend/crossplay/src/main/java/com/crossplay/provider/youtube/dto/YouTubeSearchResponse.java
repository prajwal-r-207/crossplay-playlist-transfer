package com.crossplay.provider.youtube.dto;

import java.util.List;

public class YouTubeSearchResponse {
    private List<YouTubeSearchItem> items;

    public List<YouTubeSearchItem> getItems() {
        return items;
    }

    public void setItems(List<YouTubeSearchItem> items) {
        this.items = items;
    }
}
