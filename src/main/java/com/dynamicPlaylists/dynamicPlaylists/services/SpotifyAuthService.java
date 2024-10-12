package com.dynamicPlaylists.dynamicPlaylists.services;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.UserRepository;
import com.dynamicPlaylists.dynamicPlaylists.util.AESUtil;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SpotifyAuthService {

    private final String clientId;
    private final String clientSecret;
    private final SpotifyDataService spotifyDataService;
    private final UserRepository userRepository;

    public SpotifyAuthService(SpotifyDataService spotifyDataService, UserRepository userRepository, Environment env) {
        this.spotifyDataService = spotifyDataService;
        this.userRepository = userRepository;
        this.clientId = env.getProperty("spring.security.oauth2.client.registration.spotify.client-id");
        this.clientSecret = env.getProperty("spring.security.oauth2.client.registration.spotify.client-secret");
    }

    public String getLoginUrl() {
        String redirectUri = "http://localhost:8080/callback";
        String scope = "user-read-private user-read-email playlist-read-private playlist-read-collaborative playlist-modify-public playlist-modify-private";
        String responseType = "code";

        return String.format(
                "https://accounts.spotify.com/authorize?client_id=%s&redirect_uri=%s&scope=%s&response_type=%s",
                clientId, redirectUri, scope, responseType
        );
    }

    public String handleSpotifyCallback(String code) throws Exception {
        HttpURLConnection con = prepareGetAccessTokenRequest(code);

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                saveDataToUser(response.toString());
            }
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
        return "Authentication successful!, you can now close this tab.";
    }

    @NotNull
    private HttpURLConnection prepareGetAccessTokenRequest(String code) throws IOException {
        String redirectUri = "http://localhost:8080/callback";
        URL tokenUrl = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection con = (HttpURLConnection) tokenUrl.openConnection();

        String body = String.format(
                "grant_type=authorization_code&code=%s&redirect_uri=%s",
                code, redirectUri
        );

        String clientCredentials = clientId + ":" + clientSecret;
        String encodedClientCredentials = Base64.getEncoder().encodeToString(clientCredentials.getBytes());

        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Basic " + encodedClientCredentials);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return con;
    }

    private void saveDataToUser(String response) throws Exception {
        JSONObject jsonResponse = new JSONObject(response);

        String accessToken = jsonResponse.getString("access_token");
        String refreshToken = jsonResponse.getString("refresh_token");
        String scope = jsonResponse.getString("scope");
        int expiresIn = jsonResponse.getInt("expires_in");

        JSONObject spotifyUserData = spotifyDataService.fetchSpotifyUserInfo(accessToken);
        //System.out.println("Spotify User Info Response: " + spotifyUserData.toString());

        String userId = spotifyUserData.getString("id");
        String username = spotifyUserData.getString("display_name");

        String encryptedAccessToken = AESUtil.encrypt(accessToken);
        String encryptedRefreshToken = AESUtil.encrypt(refreshToken);


        User user = userRepository.findById(userId).orElse(new User(userId, username)); //todo findbyid must be tested when db is persisting

        user.setScope(scope);
        user.setAccessToken(encryptedAccessToken);
        user.setRefreshToken(encryptedRefreshToken);
        user.setExpiresIn(System.currentTimeMillis() + (expiresIn * 1000L));

        // Save the user back to the database
        userRepository.save(user);
    }

    public void refreshAccessToken(User user) {
        try {
            HttpURLConnection con = prepareRefreshAccessTokenRequest(user.getRefreshToken());
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String accessToken = jsonResponse.getString("access_token");
                    String encryptedAccessToken = AESUtil.encrypt(accessToken);
                    user.setAccessToken(encryptedAccessToken);
                    user.setExpiresIn(System.currentTimeMillis() + (jsonResponse.getInt("expires_in") * 1000L));
                    userRepository.save(user);
                }
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    } //todo not tested working

    @NotNull
    private HttpURLConnection prepareRefreshAccessTokenRequest(String refreshToken) throws IOException {
        URL tokenUrl = new URL("https://accounts.spotify.com/api/token");
        HttpURLConnection con = (HttpURLConnection) tokenUrl.openConnection();

        String body = String.format(
                "grant_type=refresh_token&refresh_token=%s",
                refreshToken
        );

        String clientCredentials = clientId + ":" + clientSecret;
        String encodedClientCredentials = Base64.getEncoder().encodeToString(clientCredentials.getBytes());

        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Basic " + encodedClientCredentials);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return con;
    }


}