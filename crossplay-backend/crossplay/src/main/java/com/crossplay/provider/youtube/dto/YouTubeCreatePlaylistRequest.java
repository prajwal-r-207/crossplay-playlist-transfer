package com.crossplay.provider.youtube.dto;

public class YouTubeCreatePlaylistRequest {

    private Snippet snippet;
    private Status status;

    public YouTubeCreatePlaylistRequest(String title, String description,String privacy) {
        this.snippet = new Snippet(title, description);
        this.status = new Status(privacy);
    }

    public Snippet getSnippet() { return snippet; }
    public Status getStatus() { return status; }

    public static class Snippet {
        private String title;
        private String description;

        public Snippet(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    public static class Status {
        private String privacyStatus;

        public Status(String privacyStatus) {
            this.privacyStatus = privacyStatus;
        }

        public String getPrivacyStatus() { return privacyStatus; }
    }
}
