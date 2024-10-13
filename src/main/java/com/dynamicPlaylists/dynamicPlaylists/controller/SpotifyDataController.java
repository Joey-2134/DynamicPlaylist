package com.dynamicPlaylists.dynamicPlaylists.controller;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.services.SpotifyDataService;
import com.dynamicPlaylists.dynamicPlaylists.services.UserDataService;
import com.dynamicPlaylists.dynamicPlaylists.util.SpotifyUtil;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class SpotifyDataController {

    private final UserDataService userDataService;
    private final SpotifyDataService spotifyDataService;
    private final SpotifyUtil spotifyUtil;

    public SpotifyDataController(UserDataService userDataService, SpotifyDataService spotifyDataService, SpotifyUtil spotifyUtil) {
        this.userDataService = userDataService;
        this.spotifyDataService = spotifyDataService;
        this.spotifyUtil = spotifyUtil;
    }

    @GetMapping("/save-user-playlists")
    public String saveUserPlaylists(@RequestParam String userId) {
        try {
            User user = userDataService.findUserById(userId);
            String validAccessToken = spotifyUtil.getValidAccessToken(user);

            List<JSONObject> playlists = spotifyDataService.fetchSpotifyPlaylists(validAccessToken, userId);
            userDataService.saveUserPlaylists(userId, playlists, validAccessToken);

            return "Playlists and songs saved successfully!";
        } catch (IOException e) {
            return "Failed to save playlists: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
