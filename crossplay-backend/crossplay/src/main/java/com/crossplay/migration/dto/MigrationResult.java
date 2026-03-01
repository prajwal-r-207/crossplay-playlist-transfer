package com.crossplay.migration.dto;

import java.util.List;

public class MigrationResult {

    private int totalTracks;
    private int matched;
    private int skipped;

    /**
     * Names of the source tracks that could not be matched on the target platform.
     */
    private List<String> failedTracks;

    public MigrationResult(int totalTracks, int matched, int skipped, List<String> failedTracks) {
        this.totalTracks = totalTracks;
        this.matched = matched;
        this.skipped = skipped;
        this.failedTracks = failedTracks != null ? failedTracks : List.of();
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

    public List<String> getFailedTracks() {
        return failedTracks;
    }
}
