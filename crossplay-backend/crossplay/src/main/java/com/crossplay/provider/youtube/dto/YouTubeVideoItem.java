package com.crossplay.provider.youtube.dto;

/**
 * Represents a single item in the YouTube /videos API response.
 * Used to fetch contentDetails (duration) for a batch of video IDs.
 */
public class YouTubeVideoItem {

    private String id;
    private VideoContentDetails contentDetails;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VideoContentDetails getContentDetails() {
        return contentDetails;
    }

    public void setContentDetails(VideoContentDetails contentDetails) {
        this.contentDetails = contentDetails;
    }

    public static class VideoContentDetails {
        /** ISO 8601 duration string, e.g. "PT3M45S" */
        private String duration;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }
    }
}
