package com.crossplay.provider.youtube;

import com.crossplay.playlist.PlaylistService;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.youtube.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class YouTubeClient implements MusicPlatformClient {

    private final WebClient webClient;
    private static final Logger log =
            LoggerFactory.getLogger(YouTubeClient.class);

    public YouTubeClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<PlaylistDto> getPlaylists(String accessToken) {

        YouTubePlaylistResponse response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("www.googleapis.com")
                                .path("/youtube/v3/playlists")
                                .queryParam("part", "snippet,contentDetails")
                                .queryParam("mine", "true")
                                .build())
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .retrieve()
                        .bodyToMono(YouTubePlaylistResponse.class)
                        .block();

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(item -> new PlaylistDto(
                        item.getId(),
                        item.getSnippet().getTitle(),
                        "YouTube", // owner (YouTube doesn't return like Spotify)
                        item.getSnippet().getThumbnails() != null &&
                                item.getSnippet().getThumbnails().getMedium() != null
                                ? item.getSnippet().getThumbnails().getMedium().getUrl()
                                : null,
                        item.getContentDetails().getItemCount()
                ))
                .toList();
    }

    @Override
    public List<TrackDto> getPlaylistTracks(String playlistId, String accessToken) {

        YouTubePlaylistTracksResponse response =
                webClient.get()
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
                        .bodyToMono(YouTubePlaylistTracksResponse.class)
                        .block();

        if (response == null || response.getItems() == null) {
            return List.of();
        }

        return response.getItems().stream()
                .map(item -> {

                    String videoId = item.getSnippet()
                            .getResourceId()
                            .getVideoId();

                    String thumbnailUrl = null;

                    if (item.getSnippet().getThumbnails() != null &&
                            item.getSnippet().getThumbnails().getMedium() != null) {
                        thumbnailUrl =
                                item.getSnippet().getThumbnails()
                                        .getMedium()
                                        .getUrl();
                    }

                    List<String> artists = getArtists(item);

                    return new TrackDto(
                            videoId,
                            item.getSnippet().getTitle(),
                            null, // no album in YouTube
                            artists, // placeholder
                            0L, // duration not available yet
                            thumbnailUrl,
                            false // no explicit info
                    );
                })
                .toList();
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

        String privacy = isPublic ? "public" : "private";

        YouTubeCreatePlaylistRequest request =
                new YouTubeCreatePlaylistRequest(name, description, privacy);

        YouTubePlaylistItem response =
                webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .scheme("https")
                                .host("www.googleapis.com")
                                .path("/youtube/v3/playlists")
                                .queryParam("part", "snippet,status")
                                .build())
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(YouTubePlaylistItem.class)
                        .block();

        String imageUrl = null;

        if (response.getSnippet().getThumbnails() != null &&
                response.getSnippet().getThumbnails().getMedium() != null) {
            imageUrl =
                    response.getSnippet()
                            .getThumbnails()
                            .getMedium()
                            .getUrl();
        }

        return new PlaylistDto(
                response.getId(),
                response.getSnippet().getTitle(),
                response.getSnippet().getChannelTitle(),
                imageUrl,
                0 // new playlist → 0 items
        );
    }

    @Override
    public void addTracks(String playlistId,
                          List<String> videoIds,
                          String accessToken) {

        for (String videoId : videoIds) {

            YouTubeAddToPlaylistRequest request =
                    new YouTubeAddToPlaylistRequest(playlistId, videoId);

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
                    .toBodilessEntity()
                    .block();
        }
    }

    @Override
    public List<TrackDto> searchTracks(String query, String accessToken) {

        YouTubeSearchResponse searchResponse =
                webClient.get()
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
                        .bodyToMono(YouTubeSearchResponse.class)
                        .block();

        log.info("youtube searchResponse: {}",searchResponse);
        if (searchResponse == null || searchResponse.getItems() == null)
            return List.of();

        return searchResponse.getItems().stream()
                .map(item -> new TrackDto(
                        item.getId().getVideoId(),
                        item.getSnippet().getTitle(),
                        null,  // album not available
                        List.of(item.getSnippet().getChannelTitle()),
                        0,     // duration for now
                        item.getId().getVideoId(),
                        false
                ))
                .toList();
    }
}
