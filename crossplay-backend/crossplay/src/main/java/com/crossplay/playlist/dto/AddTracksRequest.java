package com.crossplay.playlist.dto;

import java.util.List;

public class AddTracksRequest {
    private List<String> trackIds;

    public List<String> getTrackIds() {
        return trackIds;
    }

    public void setTrackIds(List<String> trackIds) {
        this.trackIds = trackIds;
    }
}
