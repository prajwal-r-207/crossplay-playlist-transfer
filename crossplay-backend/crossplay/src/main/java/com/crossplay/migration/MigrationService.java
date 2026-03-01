package com.crossplay.migration;

import com.crossplay.auth.DevTokenStore;
import com.crossplay.migration.dto.MigrationRequest;
import com.crossplay.migration.dto.MigrationResult;
import com.crossplay.playlist.PlaylistService;
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
import java.util.List;

@Service
public class MigrationService {

    private final MusicPlatformFactory platformFactory;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final DevTokenStore tokenStore;
    private static final Logger log =
            LoggerFactory.getLogger(MigrationService.class);

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
            OAuth2AuthenticationToken authentication
    ) {

        // Load source + target tokens
        OAuth2AuthorizedClient sourceAuth =
                authorizedClientService.loadAuthorizedClient(
                        request.getSourcePlatform().getRegistrationId(),
                        authentication.getName()
                );

        OAuth2AuthorizedClient targetAuth =
                authorizedClientService.loadAuthorizedClient(
                        request.getTargetPlatform().getRegistrationId(),
                        authentication.getName()
                );

//        String sourceToken = sourceAuth.getAccessToken().getTokenValue();
        String sourceToken = tokenStore.getToken(request.getSourcePlatform().getRegistrationId());
//        String targetToken = targetAuth.getAccessToken().getTokenValue();
        String targetToken = tokenStore.getToken(request.getTargetPlatform().getRegistrationId());

        MusicPlatformClient sourceClient =
                platformFactory.getClient(request.getSourcePlatform());

        MusicPlatformClient targetClient =
                platformFactory.getClient(request.getTargetPlatform());

        // Fetch source tracks
        List<TrackDto> sourceTracks =
                sourceClient.getPlaylistTracks(
                        request.getSourcePlaylistId(),
                        sourceToken
                );

        if (sourceTracks == null || sourceTracks.isEmpty()) {
            return new MigrationResult(0, 0, 0);
        }

        // Create target playlist
        PlaylistDto newPlaylist =
                targetClient.createPlaylist(
                        "Migrated Playlist",
                        "Migrated from " + request.getSourcePlatform(),
                        true,
                        targetToken
                );

        List<String> targetIds = new ArrayList<>();
        int matched = 0;

        // For each track → search + match
        for (TrackDto track : sourceTracks) {

            String query = buildSearchQuery(track);
            log.info("Query:{}", query);

            List<TrackDto> candidates =
                    targetClient.searchTracks(query, targetToken);
            log.info("candidates: {}",candidates);

            TrackDto bestMatch = pickBestMatch(track, candidates);
            log.info("bestMatch: {}", bestMatch);

            if (bestMatch != null) {
                targetIds.add(bestMatch.getId());  // or videoId for YouTube
                matched++;
            }
        }

        log.info("targetIds: {}", targetIds);
        // Add tracks
        if (!targetIds.isEmpty()) {
            targetClient.addTracks(
                    newPlaylist.getId(),
                    targetIds,
                    targetToken
            );
        }

        return new MigrationResult(
                sourceTracks.size(),
                matched,
                sourceTracks.size() - matched
        );
    }

    private String buildSearchQuery(TrackDto track) {

        String artist = track.getArtists().isEmpty()
                ? ""
                : track.getArtists().get(0);

        return track.getName() + " " + artist + " official audio";
    }

    private TrackDto pickBestMatch(
            TrackDto source,
            List<TrackDto> candidates
    ) {

        if (candidates == null || candidates.isEmpty())
            return null;

        for (TrackDto candidate : candidates) {

            boolean titleMatch =
                    candidate.getName().equalsIgnoreCase(source.getName());

//            long durationDiff =
//                    Math.abs(candidate.getDurationMs() - source.getDurationMs());
//
//            if (titleMatch && durationDiff < 5000) {
//                return candidate;
//            }
            if (titleMatch) {
                return candidate;
            }
        }
        log.info("");
        return candidates.get(0); // fallback
    }
}
