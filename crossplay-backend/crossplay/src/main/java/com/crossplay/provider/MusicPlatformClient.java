package com.crossplay.provider;

import com.crossplay.playlist.dto.PlaylistDto;
import com.crossplay.playlist.dto.TrackDto;

import java.util.List;
public interface MusicPlatformClient {

    List<PlaylistDto> getPlaylists(String accessToken);
    List<TrackDto> getPlaylistTracks(String playlistId, String accessToken);
}

