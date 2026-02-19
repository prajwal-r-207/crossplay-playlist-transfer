package com.crossplay.playlist.dto;

public class PlaylistDto {
    private String id;
    private String name;
    private String owner;
    private String imageUrl;
    private int trackCount;

    public PlaylistDto(String id,
                       String name,
                       String owner,
                       String imageUrl,
                       int trackCount) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.imageUrl = imageUrl;
        this.trackCount = trackCount;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }
}
