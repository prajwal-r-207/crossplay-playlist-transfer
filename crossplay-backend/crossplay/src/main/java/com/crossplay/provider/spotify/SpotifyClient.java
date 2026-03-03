package com.crossplay.provider.spotify;

import com.crossplay.exception.ExternalApiException;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.spotify.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class SpotifyClient implements MusicPlatformClient {

        private static final Logger log = LoggerFactory.getLogger(SpotifyClient.class);
        private static final String PLATFORM = "Spotify";

        private final WebClient webClient;

        public SpotifyClient(WebClient webClient) {
                this.webClient = webClient;
        }

        @Override
        public List<PlaylistDto> getPlaylists(String accessToken) {
                log.debug("[Spotify] Fetching playlists for current user");

                SpotifyPlaylistPageResponse response = webClient.get()
                                .uri("https://api.spotify.com/v1/me/playlists")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(SpotifyPlaylistPageResponse.class)
                                .block();

                if (response == null || response.getItems() == null) {
                        log.warn("[Spotify] getPlaylists returned null or empty response");
                        return List.of();
                }

                log.info("[Spotify] Fetched {} playlists", response.getItems().size());

                return response.getItems().stream()
                                .map(item -> {
                                        String imageUrl = null;

                                        if (item.getImages() != null && !item.getImages().isEmpty()) {
                                                imageUrl = item.getImages().get(0).getUrl();
                                        }
                                        return new PlaylistDto(
                                                        item.getId(),
                                                        item.getName(),
                                                        item.getOwner().getDisplay_name(),
                                                        imageUrl,
                                                        item.getItems().getTotal());
                                })
                                .toList();
        }

        @Override
        public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
                log.debug("[Spotify] Fetching tracks for playlistId={}", playlistId);

                SpotifyPlaylistTracksResponse response = webClient.get()
                                .uri("https://api.spotify.com/v1/playlists/{playlistId}/items", playlistId)
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(body -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                body)))
                                .bodyToMono(SpotifyPlaylistTracksResponse.class)
                                .block();

                if (response == null || response.getItems() == null) {
                        log.warn("[Spotify] getPlaylistTracks returned null or empty response for playlistId={}",
                                        playlistId);
                        return List.of();
                }

                log.info("[Spotify] Fetched {} tracks for playlistId={}", response.getItems().size(), playlistId);

                return response.getItems().stream()
                                .map(item -> {
                                        SpotifyTrack track = item.getItem();

                                        return new TrackDto(
                                                        track.getId(),
                                                        track.getName(),
                                                        track.getAlbum().getName(),
                                                        track.getArtists()
                                                                        .stream()
                                                                        .map(SpotifyArtist::getName)
                                                                        .toList(),
                                                        track.getDuration_ms(),
                                                        track.getAlbum().getImages().isEmpty()
                                                                        ? null
                                                                        : track.getAlbum().getImages().get(0).getUrl(),
                                                        track.isExplicit());
                                })
                                .toList();
        }

        @Override
        public PlaylistDto createPlaylist(String name, String description, boolean isPublic, String accessToken) {
                log.debug("[Spotify] Creating playlist name='{}' isPublic={}", name, isPublic);

                Map<String, Object> body = Map.of(
                                "name", name,
                                "description", description,
                                "public", isPublic);

                SpotifyPlaylistResponse response = webClient.post()
                                .uri("https://api.spotify.com/v1/me/playlists")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(b -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                b)))
                                .bodyToMono(SpotifyPlaylistResponse.class)
                                .block();

                if (response == null) {
                        throw new ExternalApiException(PLATFORM, 200, "Null response body when creating playlist");
                }

                log.info("[Spotify] Created playlist id={} name='{}'", response.getId(), response.getName());

                return new PlaylistDto(
                                response.getId(),
                                response.getName(),
                                response.getOwner().getDisplay_name(),
                                response.getImages().isEmpty()
                                                ? null
                                                : response.getImages().get(0).getUrl(),
                                response.getItems().getTotal());
        }

        @Override
        public void addTracks(String playlistId, List<String> trackIds, String accessToken) {
                log.debug("[Spotify] Adding {} tracks to playlistId={}", trackIds.size(), playlistId);

                List<String> uris = trackIds.stream()
                                .map(id -> "spotify:track:" + id)
                                .toList();

                Map<String, Object> body = Map.of("uris", uris);

                webClient.post()
                                .uri("https://api.spotify.com/v1/playlists/{playlistId}/items", playlistId)
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(b -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                b)))
                                .bodyToMono(Void.class)
                                .block();

                log.info("[Spotify] Successfully added {} tracks to playlistId={}", trackIds.size(), playlistId);
        }

        @Override
        public List<TrackDto> searchTracks(String query, String accessToken) {
                log.debug("[Spotify] Searching tracks query='{}'", query);

                SpotifySearchResponse response = webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("api.spotify.com")
                                                .path("/v1/search")
                                                .queryParam("q", query)
                                                .queryParam("type", "track")
                                                .queryParam("limit", 5)
                                                .build())
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .onStatus(
                                                status -> !status.is2xxSuccessful(),
                                                clientResponse -> clientResponse.bodyToMono(String.class)
                                                                .map(b -> new ExternalApiException(
                                                                                PLATFORM,
                                                                                clientResponse.statusCode().value(),
                                                                                b)))
                                .bodyToMono(SpotifySearchResponse.class)
                                .block();

                if (response == null || response.getTracks() == null) {
                        log.warn("[Spotify] searchTracks returned no results for query='{}'", query);
                        return List.of();
                }

                log.info("[Spotify] Search for '{}' returned {} candidates", query,
                                response.getTracks().getItems().size());

                return response.getTracks().getItems().stream()
                                .map(track -> new TrackDto(
                                                track.getId(),
                                                track.getName(),
                                                track.getAlbum().getName(),
                                                track.getArtists().stream()
                                                                .map(SpotifyArtist::getName)
                                                                .toList(),
                                                track.getDuration_ms(),
                                                track.getUri(), // IMPORTANT for addTracks
                                                track.isExplicit()))
                                .toList();
        }
}
