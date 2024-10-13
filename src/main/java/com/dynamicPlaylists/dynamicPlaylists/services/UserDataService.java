package com.dynamicPlaylists.dynamicPlaylists.services;

import com.dynamicPlaylists.dynamicPlaylists.entity.Playlist;
import com.dynamicPlaylists.dynamicPlaylists.entity.PlaylistSong;
import com.dynamicPlaylists.dynamicPlaylists.entity.Song;
import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.PlaylistRepository;
import com.dynamicPlaylists.dynamicPlaylists.repository.PlaylistSongRepository;
import com.dynamicPlaylists.dynamicPlaylists.repository.SongRepository;
import com.dynamicPlaylists.dynamicPlaylists.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class UserDataService {
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SpotifyDataService spotifyDataService;

    public UserDataService(UserRepository userRepository, PlaylistRepository playlistRepository, SongRepository songRepository, PlaylistSongRepository playlistSongRepository, SpotifyDataService spotifyDataService) {
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.spotifyDataService = spotifyDataService;
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void saveUserPlaylists(String userId, List<JSONObject> playlists, String accessToken) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        for (JSONObject playlistJson : playlists) {
            Playlist playlist = new Playlist();
            playlist.setId(playlistJson.getString("id"));
            playlist.setName(playlistJson.getString("name"));
            playlist.setUser(user);

            // Fetch and save the songs in the playlist
            savePlaylistSongs(playlist, accessToken, playlistJson.getString("id"));

            playlistRepository.save(playlist); // Save playlist
        }
    }

    private void savePlaylistSongs(Playlist playlist, String accessToken, String playlistId) throws IOException {
        List<JSONObject> songs = spotifyDataService.fetchSpotifyPlaylistSongs(accessToken, playlistId);
        //System.out.println("Songs: " + songs.toString());
        for (JSONObject songJson : songs) {
            Song song = songRepository.findById(songJson.getString("id"))
                    .orElseGet(() -> new Song(
                            songJson.getString("id"),
                            songJson.getString("name"),
                            songJson.getJSONArray("artists").getJSONObject(0).getString("name")));

            System.out.println("Song: " + song.getName() + " by " + song.getArtist());
            songRepository.save(song); // Save song if it's new

//            // Create a PlaylistSong entry for the many-to-many relationship
//            PlaylistSong playlistSong = new PlaylistSong();
//            playlistSong.setPlaylist(playlist);
//            playlistSong.setSong(song);
//            playlistSongRepository.save(playlistSong); // Save playlist-song relationship
        }
    }
}
