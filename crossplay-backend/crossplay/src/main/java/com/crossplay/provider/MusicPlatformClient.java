package com.crossplay.provider;

import com.crossplay.playlist.dto.PlaylistDto;

import java.util.List;
public interface MusicPlatformClient {

    List<PlaylistDto> getPlaylists();
}
