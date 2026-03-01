package com.crossplay.migration;

import com.crossplay.auth.DevTokenStore;
import com.crossplay.migration.dto.MigrationRequest;
import com.crossplay.migration.dto.MigrationResult;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.MusicPlatformFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MigrationService {

        private final MusicPlatformFactory platformFactory;
        private final OAuth2AuthorizedClientService authorizedClientService;
        private final DevTokenStore tokenStore;
        private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

        /** Duration difference (ms) below which we consider it a "close" match. */
        private static final long DEFAULT_DURATION_TOLERANCE_MS = 5000L;

        public MigrationService(
                        MusicPlatformFactory platformFactory,
                        OAuth2AuthorizedClientService authorizedClientService,
                        DevTokenStore tokenStore) {
                this.platformFactory = platformFactory;
                this.authorizedClientService = authorizedClientService;
                this.tokenStore = tokenStore;
        }

        public MigrationResult migrate(
                        MigrationRequest request,
                        OAuth2AuthenticationToken authentication) {

                // Load tokens from the dev token store
                // (swap these with OAuth2AuthorizedClient once prod auth is wired up)
                String sourceToken = tokenStore.getToken(request.getSourcePlatform().getRegistrationId());
                String targetToken = tokenStore.getToken(request.getTargetPlatform().getRegistrationId());

                MusicPlatformClient sourceClient = platformFactory.getClient(request.getSourcePlatform());

                MusicPlatformClient targetClient = platformFactory.getClient(request.getTargetPlatform());

                // ── Resolve source tracks: full playlist, then filter if specific IDs
                // requested ──
                List<TrackDto> allTracks = sourceClient.getPlaylistTracks(
                                request.getSourcePlaylistId(), sourceToken);

                List<TrackDto> sourceTracks;
                if (request.getSourceTrackIds() != null && !request.getSourceTrackIds().isEmpty()) {
                        // Partial migration: filter down to only the requested track IDs
                        Set<String> requestedIds = new HashSet<>(request.getSourceTrackIds());
                        sourceTracks = allTracks.stream()
                                        .filter(t -> requestedIds.contains(t.getId()))
                                        .toList();
                        log.info("Partial migration: {} of {} tracks selected",
                                        sourceTracks.size(), allTracks.size());
                } else {
                        sourceTracks = allTracks;
                        log.info("Full playlist migration: {} tracks", sourceTracks.size());
                }
                if (sourceTracks == null || sourceTracks.isEmpty()) {
                        return new MigrationResult(0, 0, 0, List.of());
                }

                // Use caller-supplied name (frontend knows it from the UI) or fall back to
                // default
                String playlistName = (request.getTargetPlaylistName() != null
                                && !request.getTargetPlaylistName().isBlank())
                                                ? request.getTargetPlaylistName()
                                                : "Migrated Playlist using Crossplay";

                // Create target playlist
                PlaylistDto newPlaylist = targetClient.createPlaylist(
                                playlistName,
                                "Migrated from " + request.getSourcePlatform(),
                                true,
                                targetToken);

                long toleranceMs = request.getDurationToleranceMs() > 0
                                ? request.getDurationToleranceMs()
                                : DEFAULT_DURATION_TOLERANCE_MS;

                List<String> targetIds = new ArrayList<>();
                List<String> failedTracks = new ArrayList<>();
                int matched = 0;

                // For each track → search + score + match
                for (TrackDto track : sourceTracks) {

                        String query = buildSearchQuery(track);
                        log.info("Query: {}", query);

                        List<TrackDto> candidates = targetClient.searchTracks(query, targetToken);
                        log.info("candidates: {}", candidates);

                        TrackDto bestMatch = pickBestMatch(track, candidates, toleranceMs);
                        log.info("bestMatch: {}", bestMatch);

                        if (bestMatch != null) {
                                targetIds.add(bestMatch.getId());
                                matched++;
                        } else {
                                // Track could not be matched
                                String label = track.getName()
                                                + (track.getArtists().isEmpty() ? ""
                                                                : " — " + track.getArtists().get(0));
                                failedTracks.add(label);

                                if (!request.isSkipUnmatched()) {
                                        // Fail-fast mode: abort and report what was done so far
                                        log.warn("Aborting migration: no match for '{}'", label);
                                        // Add whatever we have so far, then return partial result
                                        addIfNotEmpty(targetClient, newPlaylist.getId(), targetIds, targetToken);
                                        return new MigrationResult(
                                                        sourceTracks.size(),
                                                        matched,
                                                        sourceTracks.size() - matched,
                                                        failedTracks);
                                }

                                log.warn("No match found for '{}', skipping (partial migration).", label);
                        }
                }

                log.info("targetIds: {}", targetIds);

                // Add all matched tracks in one go
                addIfNotEmpty(targetClient, newPlaylist.getId(), targetIds, targetToken);

                return new MigrationResult(
                                sourceTracks.size(),
                                matched,
                                sourceTracks.size() - matched,
                                failedTracks);
        }

        // -------------------------------------------------------------------------
        // Helpers
        // -------------------------------------------------------------------------

        private void addIfNotEmpty(MusicPlatformClient client,
                        String playlistId,
                        List<String> ids,
                        String token) {
                if (!ids.isEmpty()) {
                        client.addTracks(playlistId, ids, token);
                }
        }

        private String buildSearchQuery(TrackDto track) {
                String artist = track.getArtists().isEmpty()
                                ? ""
                                : track.getArtists().get(0);

                return track.getName() + " " + artist + " official audio";
        }

        /**
         * Picks the best matching candidate using a two-tier scoring system:
         *
         * <ol>
         * <li><b>Tier 1 (exact)</b>: title matches (case-insensitive) AND duration
         * is within {@code toleranceMs}. First such candidate wins.</li>
         * <li><b>Tier 2 (title-only)</b>: title matches but duration is unknown
         * (0) on either side, or duration differs beyond tolerance.
         * Return the closest-duration title match.</li>
         * <li><b>Fallback</b>: no title match at all → return the candidate
         * with the closest duration to the source (only if source has a
         * known duration), otherwise return the first result.</li>
         * </ol>
         *
         * @param source      the source track to match
         * @param candidates  the search results from the target platform
         * @param toleranceMs acceptable duration difference in milliseconds
         * @return best matching {@link TrackDto}, or {@code null} if candidates is
         *         empty
         */
        TrackDto pickBestMatch(TrackDto source, List<TrackDto> candidates, long toleranceMs) {
                if (candidates == null || candidates.isEmpty())
                        return null;

                boolean sourceHasDuration = source.getDurationMs() > 0;

                TrackDto bestTitleMatch = null;
                long bestTitleMatchDurationDiff = Long.MAX_VALUE;

                for (TrackDto candidate : candidates) {

                        boolean titleMatches = candidate.getName()
                                        .equalsIgnoreCase(source.getName());
                        boolean candidateHasDuration = candidate.getDurationMs() > 0;

                        long durationDiff = (sourceHasDuration && candidateHasDuration)
                                        ? Math.abs(candidate.getDurationMs() - source.getDurationMs())
                                        : Long.MAX_VALUE;

                        if (titleMatches) {
                                // Tier 1: perfect match (title + duration within tolerance)
                                if (sourceHasDuration && candidateHasDuration && durationDiff < toleranceMs) {
                                        log.info("Tier-1 match: '{}' durationDiff={}ms", candidate.getName(),
                                                        durationDiff);
                                        return candidate;
                                }

                                // Track as a title-only candidate; keep the best duration fit
                                if (durationDiff < bestTitleMatchDurationDiff) {
                                        bestTitleMatch = candidate;
                                        bestTitleMatchDurationDiff = durationDiff;
                                }
                        }
                }

                // Tier 2: a title match existed but duration didn't fit the tight window
                if (bestTitleMatch != null) {
                        log.info("Tier-2 match (title only): '{}' durationDiff={}ms",
                                        bestTitleMatch.getName(), bestTitleMatchDurationDiff);
                        return bestTitleMatch;
                }

                // Fallback: pick candidate with closest duration (or first if unknown)
                if (sourceHasDuration) {
                        TrackDto closestByDuration = candidates.stream()
                                        .filter(c -> c.getDurationMs() > 0)
                                        .min((a, b) -> {
                                                long diffA = Math.abs(a.getDurationMs() - source.getDurationMs());
                                                long diffB = Math.abs(b.getDurationMs() - source.getDurationMs());
                                                return Long.compare(diffA, diffB);
                                        })
                                        .orElse(candidates.get(0));
                        log.info("Fallback (closest duration): '{}'", closestByDuration.getName());
                        return closestByDuration;
                }

                log.info("Fallback (first result): '{}'", candidates.get(0).getName());
                return candidates.get(0);
        }
}
