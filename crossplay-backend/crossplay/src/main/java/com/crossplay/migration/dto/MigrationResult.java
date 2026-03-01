package com.crossplay.migration.dto;

import com.crossplay.common.PlatformType;

public class MigrationResult {

    private int totalTracks;
    private int matched;
    private int skipped;

    public MigrationResult(int totalTracks, int matched, int skipped) {
        this.totalTracks = totalTracks;
        this.matched = matched;
        this.skipped = skipped;
    }

    public int getTotalTracks() {
        return totalTracks;
    }

    public int getMatched() {
        return matched;
    }

    public int getSkipped() {
        return skipped;
    }
}
