package com.crossplay.provider.youtube.dto;

public class Thumbnails {

    private Thumbnail medium;
    private Thumbnail high;

    public Thumbnail getMedium() {
        return medium;
    }

    public Thumbnail getHigh() {
        return high;
    }

    public void setMedium(Thumbnail medium) {
        this.medium = medium;
    }

    public void setHigh(Thumbnail high) {
        this.high = high;
    }
}
