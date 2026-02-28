package com.crossplay.provider.youtube.dto;

public class YouTubeAddToPlaylistRequest {

    private Snippet snippet;

    public YouTubeAddToPlaylistRequest(String playlistId, String videoId) {
        this.snippet = new Snippet(playlistId, videoId);
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public static class Snippet {
        private String playlistId;
        private ResourceId resourceId;

        public Snippet(String playlistId, String videoId) {
            this.playlistId = playlistId;
            this.resourceId = new ResourceId(videoId);
        }

        public String getPlaylistId() {
            return playlistId;
        }

        public ResourceId getResourceId() {
            return resourceId;
        }
    }

    public static class ResourceId {
        private String kind = "youtube#video";
        private String videoId;

        public ResourceId(String videoId) {
            this.videoId = videoId;
        }

        public String getKind() {
            return kind;
        }

        public String getVideoId() {
            return videoId;
        }
    }
}
