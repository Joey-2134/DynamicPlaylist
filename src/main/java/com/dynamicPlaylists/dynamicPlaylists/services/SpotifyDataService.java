package com.dynamicPlaylists.dynamicPlaylists.services;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.util.AESUtil;
import com.dynamicPlaylists.dynamicPlaylists.util.DataUtil;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyDataService {

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
//            System.out.println("Playlists: " + response.toString(2));
//            System.out.println("Type of playlists: " + response.getClass());

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
}
