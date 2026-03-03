package com.crossplay.playlist;

import com.crossplay.auth.DevTokenStore;
import com.crossplay.exception.TokenNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crossplay.common.PlatformType;
import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;
import com.crossplay.provider.MusicPlatformClient;
import com.crossplay.provider.MusicPlatformFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {

        private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

        private final OAuth2AuthorizedClientService authorizedClientService;
        private final MusicPlatformFactory platformFactory;
        private final DevTokenStore tokenStore;

        public PlaylistService(OAuth2AuthorizedClientService authorizedClientService,
                        MusicPlatformFactory platformFactory, DevTokenStore tokenStore) {
                this.authorizedClientService = authorizedClientService;
                this.platformFactory = platformFactory;
                this.tokenStore = tokenStore;
        }

        public List<PlaylistDto> getPlaylists(PlatformType platform,
                        OAuth2AuthenticationToken authentication) {

                log.info("[PlaylistService] getPlaylists platform={} principal={}", platform, authentication.getName());

                authorizedClientService.loadAuthorizedClient(
                                platform.getRegistrationId(),
                                authentication.getName());

                String accessToken = tokenStore.getToken(platform.getRegistrationId());

                if (accessToken == null) {
                        log.warn("[PlaylistService] No token stored for platform={}", platform);
                        throw new TokenNotFoundException(platform.name());
                }

                MusicPlatformClient clientProvider = platformFactory.getClient(platform);
                List<PlaylistDto> playlists = clientProvider.getPlaylists(accessToken);
                log.info("[PlaylistService] Returning {} playlists for platform={}", playlists.size(), platform);
                return playlists;
        }

        public List<TrackDto> getPlaylistTracks(PlatformType platform, String playlistId,
                        OAuth2AuthenticationToken authentication) {

                log.info("[PlaylistService] getPlaylistTracks platform={} playlistId={} principal={}",
                                platform, playlistId, authentication.getName());

                authorizedClientService.loadAuthorizedClient(
                                platform.getRegistrationId(),
                                authentication.getName());

                String accessToken = tokenStore.getToken(platform.getRegistrationId());

                if (accessToken == null) {
                        log.warn("[PlaylistService] No token stored for platform={}", platform);
                        throw new TokenNotFoundException(platform.name());
                }

                MusicPlatformClient clientProvider = platformFactory.getClient(platform);
                List<TrackDto> tracks = clientProvider.getPlaylistTracks(playlistId, accessToken);
                log.info("[PlaylistService] Returning {} tracks for playlistId={} platform={}", tracks.size(),
                                playlistId, platform);
                return tracks;
        }

        public PlaylistDto createPlaylist(PlatformType platform,
                        String name,
                        String description,
                        boolean isPublic,
                        OAuth2AuthenticationToken authentication) {

                log.info("[PlaylistService] createPlaylist platform={} name='{}' isPublic={}", platform, name,
                                isPublic);

                authorizedClientService.loadAuthorizedClient(
                                platform.getRegistrationId(),
                                authentication.getName());

                String accessToken = tokenStore.getToken(platform.getRegistrationId());

                if (accessToken == null) {
                        log.warn("[PlaylistService] No token stored for platform={}", platform);
                        throw new TokenNotFoundException(platform.name());
                }

                MusicPlatformClient clientProvider = platformFactory.getClient(platform);
                return clientProvider.createPlaylist(name, description, isPublic, accessToken);
        }

        public void addTracks(PlatformType platform,
                        String playlistId,
                        List<String> trackIds,
                        OAuth2AuthenticationToken authentication) {

                log.info("[PlaylistService] addTracks platform={} playlistId={} trackCount={}", platform, playlistId,
                                trackIds.size());

                authorizedClientService.loadAuthorizedClient(
                                platform.getRegistrationId(),
                                authentication.getName());

                String accessToken = tokenStore.getToken(platform.getRegistrationId());

                if (accessToken == null) {
                        log.warn("[PlaylistService] No token stored for platform={}", platform);
                        throw new TokenNotFoundException(platform.name());
                }

                MusicPlatformClient provider = platformFactory.getClient(platform);
                provider.addTracks(playlistId, trackIds, accessToken);
                log.info("[PlaylistService] addTracks completed for playlistId={}", playlistId);
        }

        public PlaylistDto copyPlaylist(
                        PlatformType platform,
                        String sourcePlaylistId,
                        String newName,
                        OAuth2AuthenticationToken authentication) {

                log.info("[PlaylistService] copyPlaylist platform={} sourcePlaylistId={} newName='{}'",
                                platform, sourcePlaylistId, newName);

                authorizedClientService.loadAuthorizedClient(
                                platform.getRegistrationId(),
                                authentication.getName());

                String accessToken = tokenStore.getToken(platform.getRegistrationId());

                if (accessToken == null) {
                        log.warn("[PlaylistService] No token stored for platform={}", platform);
                        throw new TokenNotFoundException(platform.name());
                }

                MusicPlatformClient provider = platformFactory.getClient(platform);

                List<TrackDto> tracks = provider.getPlaylistTracks(sourcePlaylistId, accessToken);
                log.info("[PlaylistService] Fetched {} tracks from source playlistId={}", tracks.size(),
                                sourcePlaylistId);

                PlaylistDto newPlaylist = provider.createPlaylist(newName, "Copied via CrossPlay", true, accessToken);
                log.info("[PlaylistService] Created target playlist id={}", newPlaylist.getId());

                List<String> trackIds = tracks.stream().map(TrackDto::getId).toList();

                // Spotify allows max 100 tracks per request
                for (int i = 0; i < trackIds.size(); i += 100) {
                        List<String> batch = trackIds.subList(i, Math.min(i + 100, trackIds.size()));
                        provider.addTracks(newPlaylist.getId(), batch, accessToken);
                }

                log.info("[PlaylistService] copyPlaylist complete: {} tracks copied to playlistId={}", trackIds.size(),
                                newPlaylist.getId());
                return newPlaylist;
        }
}
