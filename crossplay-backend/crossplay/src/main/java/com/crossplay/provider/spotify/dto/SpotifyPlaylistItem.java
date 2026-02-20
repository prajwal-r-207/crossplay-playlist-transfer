package com.crossplay.provider.spotify.dto;

import java.util.List;

public class SpotifyPlaylistItem {
    private String id;
    private String name;
    private SpotifyOwner owner;
    private List<SpotifyImage> images;
    private SpotifyItem items;

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

    public SpotifyOwner getOwner() {
        return owner;
    }

    public void setOwner(SpotifyOwner owner) {
        this.owner = owner;
    }

    public List<SpotifyImage> getImages() {
        return images;
    }

    public void setImages(List<SpotifyImage> images) {
        this.images = images;
    }

    public SpotifyItem getItems() {
        return items;
    }

    public void setItems(SpotifyItem items) {
        this.items = items;
    }
}
