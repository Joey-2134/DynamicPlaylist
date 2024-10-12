package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
}
