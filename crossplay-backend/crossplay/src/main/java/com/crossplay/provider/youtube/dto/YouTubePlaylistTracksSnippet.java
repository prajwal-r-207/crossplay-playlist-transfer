package com.crossplay.provider.youtube.dto;

public class YouTubePlaylistTracksSnippet {

    private String title;
    private ResourceId resourceId;
    private Thumbnails thumbnails;
    private String videoOwnerChannelTitle;

    public String getTitle() {
        return title;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }

    public ResourceId getResourceId() {
        return resourceId;
    }

    public String getVideoOwnerChannelTitle() {
        return videoOwnerChannelTitle;
    }

    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void setVideoOwnerChannelTitle(String videoOwnerChannelTitle) {
        this.videoOwnerChannelTitle = videoOwnerChannelTitle;
    }
}
