package com.crossplay.provider.spotify;

import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.spotify.dto.SpotifyArtist;
import  com.crossplay.provider.spotify.dto.SpotifyPlaylistResponse;
import com.crossplay.provider.spotify.dto.SpotifyPlaylistTracksResponse;
import com.crossplay.provider.spotify.dto.SpotifyTrack;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class SpotifyClient implements MusicPlatformClient {

    private final WebClient webClient;

    public SpotifyClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<PlaylistDto> getPlaylists(String accessToken) {

        SpotifyPlaylistResponse response =  webClient.get()
                .uri("https://api.spotify.com/v1/me/playlists")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(SpotifyPlaylistResponse.class)
                .block();

        return response.getItems().stream()
                .map(item -> new PlaylistDto(
                        item.getId(),
                        item.getName(),
                        item.getOwner().getDisplay_name(),
                        item.getImages().isEmpty() ? null : item.getImages().get(0).getUrl(),
                        item.getItems().getTotal()
                ))
                .toList();
    }

    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {
        SpotifyPlaylistTracksResponse response = webClient.get().
                uri("https://api.spotify.com/v1/playlists/{playlistId}/items", playlistId)
                .header("Authorization", "Bearer " + accessToken)
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
                            track.isExplicit()
                    );
                })
                .toList();
    }
}
