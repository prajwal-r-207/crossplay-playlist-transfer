package com.crossplay.provider.youtube.dto;

import java.util.List;

/**
 * Maps the YouTube Data API v3 /videos response.
 * Used to fetch contentDetails (duration) for a batch of video IDs.
 */
public class YouTubeVideoResponse {

    private List<YouTubeVideoItem> items;

    public List<YouTubeVideoItem> getItems() {
        return items;
    }

    public void setItems(List<YouTubeVideoItem> items) {
        this.items = items;
    }
}
