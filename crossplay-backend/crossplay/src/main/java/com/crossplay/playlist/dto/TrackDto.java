package com.crossplay.playlist.dto;

import java.util.List;

public class TrackDto {

    private String id;
    private String name;
    private String albumName;
    private List<String> artists;
    private int durationMs;
    private String imageUrl;
    private boolean explicit;

    public TrackDto(String id,
                    String name,
                    String albumName,
                    List<String> artists,
                    int durationMs,
                    String imageUrl,
                    boolean explicit) {
        this.id = id;
        this.name = name;
        this.albumName = albumName;
        this.artists = artists;
        this.durationMs = durationMs;
        this.imageUrl = imageUrl;
        this.explicit = explicit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public List<String> getArtists() {
        return artists;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isExplicit() {
        return explicit;
    }
}

