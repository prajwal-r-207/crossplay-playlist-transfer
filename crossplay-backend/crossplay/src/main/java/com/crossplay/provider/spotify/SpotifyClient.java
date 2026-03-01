package com.crossplay.provider.spotify;

import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.spotify.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class SpotifyClient implements MusicPlatformClient {

        private final WebClient webClient;

        public SpotifyClient(WebClient webClient) {
                this.webClient = webClient;
        }

        @Override
        public List<PlaylistDto> getPlaylists(String accessToken) {

                SpotifyPlaylistPageResponse response = webClient.get()
                                .uri("https://api.spotify.com/v1/me/playlists")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(SpotifyPlaylistPageResponse.class)
                                .block();

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
                SpotifyPlaylistTracksResponse response = webClient.get()
                                .uri("https://api.spotify.com/v1/playlists/{playlistId}/items", playlistId)
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(SpotifyPlaylistTracksResponse.class)
                                .block();

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
                Map<String, Object> body = Map.of(
                                "name", name,
                                "description", description,
                                "public", isPublic);

                SpotifyPlaylistResponse response = webClient.post()
                                .uri("https://api.spotify.com/v1/me/playlists")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(SpotifyPlaylistResponse.class)
                                .block();

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
                List<String> uris = trackIds.stream()
                                .map(id -> "spotify:track:" + id)
                                .toList();

                Map<String, Object> body = Map.of("uris", uris);

                webClient.post()
                                .uri("https://api.spotify.com/v1/playlists/{playlistId}/items", playlistId)
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .bodyValue(body)
                                .retrieve()
                                .bodyToMono(Void.class)
                                .block();
        }

        @Override
        public List<TrackDto> searchTracks(String query, String accessToken) {

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
                                .bodyToMono(SpotifySearchResponse.class)
                                .block();

                if (response == null || response.getTracks() == null)
                        return List.of();

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
