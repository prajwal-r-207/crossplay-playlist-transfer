package com.crossplay.common;

public enum PlatformType {
    SPOTIFY("spotify"),
    YOUTUBE("google"),
    APPLE("apple");

    private final String registrationId;

    PlatformType(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }
}
