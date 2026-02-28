package com.crossplay.provider.youtube.dto;

public class YouTubeSearchItem {
    private Id id;
    private YouTubeSearchSnippet snippet;

    public static class Id {
        private String videoId;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }
    }

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public YouTubeSearchSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(YouTubeSearchSnippet snippet) {
        this.snippet = snippet;
    }
}
