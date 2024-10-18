package com.dynamicPlaylists.dynamicPlaylists.services;

import com.dynamicPlaylists.dynamicPlaylists.entity.PlaylistSong;
import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.PlaylistRepository;
import com.dynamicPlaylists.dynamicPlaylists.repository.PlaylistSongRepository;
import com.dynamicPlaylists.dynamicPlaylists.repository.UserRepository;
import com.dynamicPlaylists.dynamicPlaylists.util.AESUtil;
import com.dynamicPlaylists.dynamicPlaylists.util.DataUtil;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class SpotifyPlaybackService {
    private final UserRepository userRepository;
    private final SpotifyDataService spotifyDataService;
    private final PlaylistSongRepository playlistSongRepository;

    public SpotifyPlaybackService(UserRepository userRepository, SpotifyDataService spotifyDataService, PlaylistSongRepository playlistSongRepository, PlaylistRepository playlistRepository) {
        this.userRepository = userRepository;
        this.spotifyDataService = spotifyDataService;
        this.playlistSongRepository = playlistSongRepository;
    }

    public void updateUserTrackDetails(User user) {
            try {
                JSONObject trackDetails = spotifyDataService.fetchSpotifyTrackDetails(user);
                user.setLastTrackId(trackDetails.getJSONObject("item").getString("id"));
                user.setLastTrackLength(trackDetails.getJSONObject("item").getInt("duration_ms"));
                user.setLastTrackProgress(trackDetails.getInt("progress_ms"));
                userRepository.save(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Scheduled(fixedRate = 5000)
    public void checkForSkip() {
        float skipThreshold = 0.5f;
        List<User> users = userRepository.findAllByIsPlaying(true);
        for (User user : users) {
            try {

                JSONObject trackDetails = spotifyDataService.fetchSpotifyTrackDetails(user); //get currently playing song details
                if (!user.getLastTrackId().equals(trackDetails.getJSONObject("item").getString("id"))) { //if the song has changed
                    float progress = user.getLastTrackProgress() / user.getLastTrackLength(); //calculate progress
                    Optional<PlaylistSong> playlistSong = playlistSongRepository.findBySongIdAndPlaylistId(user.getLastTrackId(), getCurrentPlaylistId(trackDetails)); //get the playlist song
                    if (progress < skipThreshold) { //if the song was skipped
                        //update tally
                        if (playlistSong.isPresent()) {
                            playlistSong.get().setTally(playlistSong.get().getTally() + 1);
                            System.out.println("Song Skipped: " + playlistSong.get().getSong().getName() + " In Playlist: " + playlistSong.get().getPlaylist().getName());
                            System.out.println("Tally Incremented: " + playlistSong.get().getTally());
                            playlistSongRepository.save(playlistSong.get());
                        }
                    } else {
                        //update tally
                        if (playlistSong.isPresent()) {
                            playlistSong.get().setTally(playlistSong.get().getTally() - 1);
                            System.out.println("Song Not Skipped: " + playlistSong.get().getSong().getName() + " In Playlist: " + playlistSong.get().getPlaylist().getName());
                            System.out.println("Tally Decremented: " + playlistSong.get().getTally());
                            playlistSongRepository.save(playlistSong.get());
                        }
                    }
                }
                updateUserTrackDetails(user);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentPlaylistId(JSONObject trackDetails) {
        return trackDetails.getJSONObject("context").getString("uri").split(":")[2];
    }
}
