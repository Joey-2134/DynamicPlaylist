package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Playlist findByName(String name);
}
