package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, String> {
}
