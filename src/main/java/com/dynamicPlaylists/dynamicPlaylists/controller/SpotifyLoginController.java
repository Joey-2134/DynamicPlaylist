package com.dynamicPlaylists.dynamicPlaylists.controller;

import com.dynamicPlaylists.dynamicPlaylists.services.SpotifyAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class SpotifyLoginController {

    private final SpotifyAuthService spotifyAuthService;

    public SpotifyLoginController(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

    @GetMapping("/api/login-url")
    public void getLoginUrl(HttpServletResponse response) throws IOException {
        String loginUrl = spotifyAuthService.getLoginUrl();
        response.sendRedirect(loginUrl);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/callback")
    public String handleSpotifyCallback(@RequestParam String code) throws Exception {
        return spotifyAuthService.handleSpotifyCallback(code);
    }





}
