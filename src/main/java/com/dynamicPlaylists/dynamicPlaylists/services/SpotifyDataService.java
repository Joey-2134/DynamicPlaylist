package com.dynamicPlaylists.dynamicPlaylists.services;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SpotifyDataService {

    JSONObject fetchSpotifyInfo(String accessToken) throws IOException {
        URL url = new URL("https://api.spotify.com/v1/me");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return new JSONObject(response.toString());
            }
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }

}
