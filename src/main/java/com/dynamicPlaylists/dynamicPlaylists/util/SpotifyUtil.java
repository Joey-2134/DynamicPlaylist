package com.dynamicPlaylists.dynamicPlaylists.util;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.services.SpotifyAuthService;
import org.springframework.stereotype.Component;

@Component
public class SpotifyUtil {

    private final SpotifyAuthService spotifyAuthService;

    public SpotifyUtil(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

    public boolean isAccessTokenCloseToExpiry(User user) {
        long currentTime = System.currentTimeMillis();
        long bufferTime = 5 * 60 * 1000L;  // 5 minutes in milliseconds
        return user.getExpiresIn() <= (currentTime + bufferTime);  // Token will expire within 5 minutes
    }

    public String getValidAccessToken(User user) throws Exception {
        // Check if the access token is close to expiry and refresh it if necessary
        if (isAccessTokenCloseToExpiry(user)) {
            System.out.println("Access token is about to expire, refreshing...");
            spotifyAuthService.refreshAccessToken(user);  // Refresh the access token
        }
        System.out.println("TOKEN CHECKED");
        //System.out.println("Access token: " + AESUtil.decrypt(user.getAccessToken()));
        // Return the (possibly refreshed) access token
        return AESUtil.decrypt(user.getAccessToken());
    }
}
