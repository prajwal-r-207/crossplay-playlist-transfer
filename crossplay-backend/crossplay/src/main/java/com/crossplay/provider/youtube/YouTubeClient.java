package com.crossplay.provider.youtube;

import com.crossplay.exception.ExternalApiException;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.youtube.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class YouTubeClient implements MusicPlatformClient {

        private final WebClient webClient;
        private static final Logger log = LoggerFactory.getLogger(YouTubeClient.class);
        private static final String PLATFORM = "YouTube";

        public YouTubeClient(WebClient webClient) {
                this.webClient = webClient;
        }

        @Override
        public List<PlaylistDto> getPlaylists(String accessToken) {
                log.debug("[YouTube] Fetching playlists for current user");

                YouTubePlaylistResponse response = webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("www.googleapis.com")
                                                .path("/youtube/v3/playlists")
                                                .queryParam("part", "snippet,contentDetails")
                                                .queryParam("mine", "true")
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(YouTubePlaylistResponse.class)
                                .block();

                if (response == null || response.getItems() == null) {
                        log.warn("[YouTube] getPlaylists returned null or empty response");
                        return List.of();
                }

                log.info("[YouTube] Fetched {} playlists", response.getItems().size());

                return response.getItems().stream()
                                .map(item -> new PlaylistDto(
                                                item.getId(),
                                                item.getSnippet().getTitle(),
                                                "YouTube",
                                                item.getSnippet().getThumbnails() != null &&
                                                                item.getSnippet().getThumbnails().getMedium() != null
                                                                                ? item.getSnippet().getThumbnails()
                                                                                                .getMedium().getUrl()
                                                                                : null,
                                                item.getContentDetails().getItemCount()))
                                .toList();
        }

        @Override
        public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
                log.debug("[YouTube] Fetching tracks for playlistId={}", playlistId);

                YouTubePlaylistTracksResponse response = webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("www.googleapis.com")
                                                .path("/youtube/v3/playlistItems")
                                                .queryParam("part", "snippet,contentDetails")
                                                .queryParam("playlistId", playlistId)
                                                .queryParam("maxResults", 50)
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(YouTubePlaylistTracksResponse.class)
                                .block();

                if (response == null || response.getItems() == null) {
                        log.warn("[YouTube] getPlaylistTracks returned null or empty for playlistId={}", playlistId);
                        return List.of();
                }

                log.info("[YouTube] Fetched {} tracks for playlistId={}", response.getItems().size(), playlistId);

                return response.getItems().stream().map(item -> {

                        String videoId = item.getSnippet()
                                        .getResourceId()
                                        .getVideoId();

                        String thumbnailUrl = null;

                        if (item.getSnippet().getThumbnails() != null &&
                                        item.getSnippet().getThumbnails().getMedium() != null) {
                                thumbnailUrl = item.getSnippet().getThumbnails()
                                                .getMedium()
                                                .getUrl();
                        }

                        List<String> artists = getArtists(item);

                        return new TrackDto(
                                        videoId,
                                        item.getSnippet().getTitle(),
                                        null,
                                        artists,
                                        0L,
                                        thumbnailUrl,
                                        false);
                }).toList();
        }

        private static List<String> getArtists(YouTubePlaylistTrackItemResponse item) {
                String channelTitle = item.getSnippet().getVideoOwnerChannelTitle();

                String artistName = null;

                if (channelTitle != null) {
                        if (channelTitle.endsWith(" - Topic")) {
                                artistName = channelTitle.replace(" - Topic", "");
                        } else {
                                artistName = channelTitle;
                        }
                }

                return artistName != null
                                ? List.of(artistName)
                                : List.of("Unknown");
        }

        @Override
        public PlaylistDto createPlaylist(String name,
                        String description,
                        boolean isPublic,
                        String accessToken) {

                log.debug("[YouTube] Creating playlist name='{}' isPublic={}", name, isPublic);

                String privacy = isPublic ? "public" : "private";

                YouTubeCreatePlaylistRequest request = new YouTubeCreatePlaylistRequest(name, description, privacy);

                YouTubePlaylistItem response = webClient.post()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("www.googleapis.com")
                                                .path("/youtube/v3/playlists")
                                                .queryParam("part", "snippet,status")
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(request)
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(YouTubePlaylistItem.class)
                                .block();

                if (response == null) {
                        throw new ExternalApiException(PLATFORM, 200, "Null response body when creating playlist");
                }

                log.info("[YouTube] Created playlist id={} name='{}'", response.getId(),
                                response.getSnippet().getTitle());

                String imageUrl = null;

                if (response.getSnippet().getThumbnails() != null &&
                                response.getSnippet().getThumbnails().getMedium() != null) {
                        imageUrl = response.getSnippet()
                                        .getThumbnails()
                                        .getMedium()
                                        .getUrl();
                }

                return new PlaylistDto(
                                response.getId(),
                                response.getSnippet().getTitle(),
                                response.getSnippet().getChannelTitle(),
                                imageUrl,
                                0);
        }

        @Override
        public void addTracks(String playlistId,
                        List<String> videoIds,
                        String accessToken) {

                log.debug("[YouTube] Adding {} tracks to playlistId={}", videoIds.size(), playlistId);

                for (String videoId : videoIds) {

                        YouTubeAddToPlaylistRequest request = new YouTubeAddToPlaylistRequest(playlistId, videoId);

                        webClient.post()
                                        .uri(uriBuilder -> uriBuilder
                                                        .scheme("https")
                                                        .host("www.googleapis.com")
                                                        .path("/youtube/v3/playlistItems")
                                                        .queryParam("part", "snippet")
                                                        .build())
                                        .headers(headers -> headers.setBearerAuth(accessToken))
                                        .bodyValue(request)
                                        .retrieve()
                                        .onStatus(
                                                        status -> !status.is2xxSuccessful(),
                                                        clientResponse -> clientResponse.bodyToMono(String.class)
                                                                        .map(body -> new ExternalApiException(
                                                                                        PLATFORM,
                                                                                        clientResponse.statusCode()
                                                                                                        .value(),
                                                                                        body)))
                                        .toBodilessEntity()
                                        .block();

                        log.debug("[YouTube] Added videoId={} to playlistId={}", videoId, playlistId);
                }

                log.info("[YouTube] Successfully added {} tracks to playlistId={}", videoIds.size(), playlistId);
        }

        @Override
        public List<TrackDto> searchTracks(String query, String accessToken) {
                log.debug("[YouTube] Searching tracks query='{}'", query);

                YouTubeSearchResponse searchResponse = webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("www.googleapis.com")
                                                .path("/youtube/v3/search")
                                                .queryParam("part", "snippet")
                                                .queryParam("type", "video")
                                                .queryParam("q", query)
                                                .queryParam("maxResults", 5)
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(YouTubeSearchResponse.class)
                                .block();

                if (searchResponse == null || searchResponse.getItems() == null) {
                        log.warn("[YouTube] searchTracks returned no results for query='{}'", query);
                        return List.of();
                }

                log.info("[YouTube] Search for '{}' returned {} candidates", query, searchResponse.getItems().size());

                // Collect video IDs for a single batch duration call
                List<String> videoIds = searchResponse.getItems().stream()
                                .map(item -> item.getId().getVideoId())
                                .toList();

                // One extra API call to get real durations for all candidates
                Map<String, Long> durationMap = fetchDurations(videoIds, accessToken);

                return searchResponse.getItems().stream()
                                .map(item -> {
                                        String videoId = item.getId().getVideoId();
                                        long durationMs = durationMap.getOrDefault(videoId, 0L);
                                        return new TrackDto(
                                                        videoId,
                                                        item.getSnippet().getTitle(),
                                                        null,
                                                        List.of(item.getSnippet().getChannelTitle()),
                                                        durationMs,
                                                        videoId,
                                                        false);
                                })
                                .toList();
        }

        /**
         * Batch-fetches durations for up to 50 video IDs via the YouTube
         * Data API v3 /videos?part=contentDetails endpoint.
         *
         * @return map of videoId → duration in milliseconds
         */
        private Map<String, Long> fetchDurations(List<String> videoIds, String accessToken) {
                if (videoIds == null || videoIds.isEmpty()) {
                        return Map.of();
                }

                String idParam = String.join(",", videoIds);

                YouTubeVideoResponse videoResponse = webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("www.googleapis.com")
                                                .path("/youtube/v3/videos")
                                                .queryParam("part", "contentDetails")
                                                .queryParam("id", idParam)
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(YouTubeVideoResponse.class)
                                .block();

                if (videoResponse == null || videoResponse.getItems() == null) {
                        log.warn("[YouTube] fetchDurations returned null or empty for ids={}", idParam);
                        return Map.of();
                }

                Map<String, Long> result = new HashMap<>();
                for (YouTubeVideoItem item : videoResponse.getItems()) {
                        if (item.getContentDetails() != null && item.getContentDetails().getDuration() != null) {
                                long ms = parseIso8601Duration(item.getContentDetails().getDuration());
                                result.put(item.getId(), ms);
                                log.debug("[YouTube] duration: videoId={} iso8601={} ms={}",
                                                item.getId(), item.getContentDetails().getDuration(), ms);
                        }
                }
                return result;
        }

        /**
         * Parses an ISO 8601 duration string (e.g. "PT3M45S", "PT1H2M3S", "PT45S")
         * into milliseconds.
         */
        static long parseIso8601Duration(String iso8601) {
                if (iso8601 == null || iso8601.isBlank())
                        return 0L;

                Matcher m = Pattern
                                .compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
                                .matcher(iso8601);

                if (!m.matches())
                        return 0L;

                long hours = m.group(1) != null ? Long.parseLong(m.group(1)) : 0L;
                long minutes = m.group(2) != null ? Long.parseLong(m.group(2)) : 0L;
                long seconds = m.group(3) != null ? Long.parseLong(m.group(3)) : 0L;

                return (hours * 3600 + minutes * 60 + seconds) * 1000L;
        }
}
