package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    Optional<PlaylistSong> findBySongIdAndPlaylistId(String songId, String playlistId);
}
