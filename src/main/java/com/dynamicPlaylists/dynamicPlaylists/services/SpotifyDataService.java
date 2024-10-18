package com.dynamicPlaylists.dynamicPlaylists.services;

import com.dynamicPlaylists.dynamicPlaylists.entity.Playlist;
import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.PlaylistRepository;
import com.dynamicPlaylists.dynamicPlaylists.util.AESUtil;
import com.dynamicPlaylists.dynamicPlaylists.util.DataUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyDataService {

    private final PlaylistRepository playlistRepository;

    public SpotifyDataService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    //fetch user info
    JSONObject fetchSpotifyUserInfo(String accessToken) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/me");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //System.out.println("Raw Spotify Response: " + response.toString());

            return new JSONObject(response.toString());
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }

    public boolean setIsPlaying(User user) throws Exception {
        URL url = new URL("https://api.spotify.com/v1/me/player");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + AESUtil.decrypt(user.getAccessToken()));

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJSON = new JSONObject(response.toString());
            //System.out.println("Playback Status Raw Spotify Response: " + response.toString());
            System.out.printf("User %s is playing\n" , user.getId());
            return responseJSON.getBoolean("is_playing");

        } else if (responseCode == 204) {
            System.out.printf("User %s is not playing\n", user.getId());
            return false;
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }

    //fetch all playlists of the user and only save the playlists that are owned by the user
    public List<JSONObject> fetchSpotifyPlaylists(String accessToken, String userId) throws IOException {

        List<JSONObject> userPlaylists = new ArrayList<>();
        URL url = new URL("https://api.spotify.com/v1/me/playlists");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject response = new JSONObject(DataUtil.parseHTTPResponse(con));
            for (int i = 0; i < response.getJSONArray("items").length(); i++) {
                JSONObject playlist = response.getJSONArray("items").getJSONObject(i);
                if (playlist.getJSONObject("owner").getString("id").equals(userId)) {
                    userPlaylists.add(playlist);
                }
            }
            return userPlaylists;

        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }

    //fetch all songs in a playlist
    public List<JSONObject> fetchSpotifyPlaylistSongs(String accessToken, String playlistId, int limit, int offset) throws IOException {
        List<JSONObject> playlistSongs = new ArrayList<>();
        String urlString = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks?limit=" + limit + "&offset=" + offset;
        URL url = new URL(urlString);
        System.out.println("Fetching from URL: " + url);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject response = new JSONObject(DataUtil.parseHTTPResponse(con));
            //System.out.println("Songs: " + response.toString(2));
            for (int i = 0; i < response.getJSONArray("items").length(); i++) {
                JSONObject song = response.getJSONArray("items").getJSONObject(i).getJSONObject("track");
                playlistSongs.add(song);
            }
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
        return playlistSongs;
    }

    public JSONObject fetchSpotifyTrackDetails(User user) {
        try {
            URL url = new URL("https://api.spotify.com/v1/me/player/currently-playing");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + AESUtil.decrypt(user.getAccessToken()));

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return new JSONObject(DataUtil.parseHTTPResponse(con));
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch track details: " + e.getMessage());
        }
    }

    public void addTrackToPlaylist(String songToAddId, String playlistToAddToId, User user) {
        try {
            URL url = new URL("https://api.spotify.com/v1/playlists/" + playlistToAddToId + "/tracks");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + AESUtil.decrypt(user.getAccessToken()));
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String body = String.format("{\"uris\":[\"spotify:track:%s\"]}", songToAddId);
            int byteLength = body.getBytes().length;
            con.setRequestProperty("Content-Length", String.valueOf(byteLength));

            // Write the body to the output stream
            con.getOutputStream().write(body.getBytes());

            if (con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Track added to playlist successfully");
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + con.getResponseCode());
            }

    } catch (Exception e) {
        throw new RuntimeException("Failed to add track to playlist: " + e.getMessage());
    }
}

    public void removeTrackFromPlaylist(String songToRemoveId, String playlistToRemoveFromId, User user) {
        try {
            URL url = new URL("https://api.spotify.com/v1/playlists/" + playlistToRemoveFromId + "/tracks");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Authorization", "Bearer " + AESUtil.decrypt(user.getAccessToken()));
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String body = String.format("{\"tracks\":[{\"uri\":\"spotify:track:%s\"}]}", songToRemoveId);
            int byteLength = body.getBytes().length;
            con.setRequestProperty("Content-Length", String.valueOf(byteLength));

            // Write the body to the output stream
            con.getOutputStream().write(body.getBytes());

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                System.out.println("Track removed from playlist successfully");
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + con.getResponseCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove track from playlist: " + e.getMessage());
        }
    }

    public void createTempPlaylists(User user, List<JSONObject> playlists) throws Exception {
        for (JSONObject playlistJson : playlists) {
            if (playlistJson.getString("name").contains("Temp")) {
                continue; // Skip if it's already a Temp playlist
            }

            // URL to create a new playlist on Spotify
            URL url = new URL("https://api.spotify.com/v1/users/" + user.getId() + "/playlists");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + AESUtil.decrypt(user.getAccessToken()));
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String body = String.format("{\"name\":\"%s\",\"public\":false}", playlistJson.getString("name") + " Temp");
            int byteLength = body.getBytes(StandardCharsets.UTF_8).length;
            con.setRequestProperty("Content-Length", String.valueOf(byteLength));

            // Write the body to the output stream
            OutputStream os = con.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
            os.close();

            // Capture the response from Spotify
            StringBuilder response = new StringBuilder();
            if (con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                // Parse the response JSON to extract the new playlist ID
                JSONObject responseObject = new JSONObject(response.toString());
                String newPlaylistId = responseObject.getString("id"); // Get the new playlist ID from the response

                System.out.println("Temp playlist created successfully with ID: " + newPlaylistId);

                // Save the new temp playlist to your database
                Playlist tempPlaylist = new Playlist();
                tempPlaylist.setId(newPlaylistId); // Use the ID from the response
                tempPlaylist.setName(playlistJson.getString("name") + " Temp");
                tempPlaylist.setUser(user);

                playlistRepository.save(tempPlaylist);

                Playlist tempPlaylistPrint = playlistRepository.findByName(playlistJson.getString("name") + " Temp");
                if (tempPlaylistPrint != null) {
                    System.out.println("Temp Playlist Saved To DB: " + tempPlaylistPrint.getName());
                } else {
                    System.out.println("Temp Playlist was not saved to DB.");
                }

            } else {
                throw new RuntimeException("Failed : HTTP error code : " + con.getResponseCode());
            }
        }
    }

}
