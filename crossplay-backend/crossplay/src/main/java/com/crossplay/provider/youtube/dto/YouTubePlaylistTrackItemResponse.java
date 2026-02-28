package com.crossplay.provider.youtube.dto;

public class YouTubePlaylistTrackItemResponse {
    private YouTubePlaylistTracksSnippet snippet;
    private ContentDetails contentDetails;

    public YouTubePlaylistTracksSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(YouTubePlaylistTracksSnippet snippet) {
        this.snippet = snippet;
    }

    public ContentDetails getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(ContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }
}
