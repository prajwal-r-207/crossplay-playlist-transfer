package com.crossplay.migration.dto;

import com.crossplay.common.PlatformType;

public class MigrationRequest {

    private PlatformType sourcePlatform;
    private PlatformType targetPlatform;
    private String sourcePlaylistId;

    public PlatformType getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(PlatformType sourcePlatform) {
        this.sourcePlatform = sourcePlatform;
    }

    public PlatformType getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(PlatformType targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public String getSourcePlaylistId() {
        return sourcePlaylistId;
    }

    public void setSourcePlaylistId(String sourcePlaylistId) {
        this.sourcePlaylistId = sourcePlaylistId;
    }
}
