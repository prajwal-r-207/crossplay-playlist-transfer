package com.crossplay.migration.dto;

import com.crossplay.common.PlatformType;

import java.util.List;

public class MigrationRequest {

    private PlatformType sourcePlatform;
    private PlatformType targetPlatform;
    private String sourcePlaylistId;

    /**
     * Optional: migrate only these specific track IDs from the source platform.
     * When non-empty, takes priority over {@code sourcePlaylistId} for track
     * selection.
     * The playlist ID is still used to derive the default target playlist name.
     */
    private List<String> sourceTrackIds;

    /**
     * Optional custom name for the new playlist.
     * Defaults to the source playlist's real name (fetched from the source
     * platform).
     */
    private String targetPlaylistName;

    /**
     * When true (default), tracks with no match are skipped and the migration
     * continues — a partial result is returned.
     * When false, the migration fails fast on the first unmatched track.
     */
    private boolean skipUnmatched = true;

    /**
     * How many milliseconds of difference in duration are still acceptable
     * for a match. Defaults to 5000 ms (5 seconds).
     */
    private long durationToleranceMs = 5000L;

    // ---- getters / setters ----

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

    public List<String> getSourceTrackIds() {
        return sourceTrackIds;
    }

    public void setSourceTrackIds(List<String> sourceTrackIds) {
        this.sourceTrackIds = sourceTrackIds;
    }

    public String getTargetPlaylistName() {
        return targetPlaylistName;
    }

    public void setTargetPlaylistName(String targetPlaylistName) {
        this.targetPlaylistName = targetPlaylistName;
    }

    public boolean isSkipUnmatched() {
        return skipUnmatched;
    }

    public void setSkipUnmatched(boolean skipUnmatched) {
        this.skipUnmatched = skipUnmatched;
    }

    public long getDurationToleranceMs() {
        return durationToleranceMs;
    }

    public void setDurationToleranceMs(long durationToleranceMs) {
        this.durationToleranceMs = durationToleranceMs;
    }
}
