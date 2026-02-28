package com.crossplay.provider.youtube.dto;

public class YouTubePlaylistItem {

    private String id;
    private YouTubePlaylistSnippet snippet;
    private ContentDetails contentDetails;

    public String getId() {
        return id;
    }

    public YouTubePlaylistSnippet getSnippet() {
        return snippet;
    }

    public ContentDetails getContentDetails() {
        return contentDetails;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSnippet(YouTubePlaylistSnippet snippet) {
        this.snippet = snippet;
    }

    public void setContentDetails(ContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }
}
