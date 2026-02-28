package com.crossplay.provider.youtube.dto;

public class YouTubePlaylistSnippet {
    private String title;
    private Thumbnails thumbnails;
    private String channelTitle;

    public String getTitle() {
        return title;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setChannelTitle(String channelTitle) {
        this.channelTitle = channelTitle;
    }
}
