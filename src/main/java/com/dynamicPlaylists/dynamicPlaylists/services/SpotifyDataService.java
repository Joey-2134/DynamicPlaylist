package com.dynamicPlaylists.dynamicPlaylists.services;

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

    //fetch all playlists of the user and only save the playlists that are owned by the user
    public List<JSONObject> fetchSpotifyPlaylists(String accessToken, String userId) throws IOException {
        List<JSONObject> userPlaylists = new ArrayList<>();
        URL url = new URL("https://api.spotify.com/v1/me/playlists");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject playlists = new JSONObject(DataUtil.parseHTTPResponse(con));
            for (int i = 0; i < playlists.getJSONArray("items").length(); i++) {
                JSONObject playlist = playlists.getJSONArray("items").getJSONObject(i);
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
    public List<JSONObject> fetchSpotifyPlaylistSongs(String accessToken, String playlistId) throws IOException {
        List<JSONObject> playlistSongs = new ArrayList<>();
        URL url = new URL("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JSONObject songs = new JSONObject(DataUtil.parseHTTPResponse(con));
            for (int i = 0; i < songs.getJSONArray("items").length(); i++) {
                JSONObject song = songs.getJSONArray("items").getJSONObject(i).getJSONObject("track");
                playlistSongs.add(song);
            }
            return playlistSongs;
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }
}
