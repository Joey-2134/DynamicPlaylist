package com.dynamicPlaylists.dynamicPlaylists.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SpotifyPlaybackService {

    @Scheduled(fixedRate = 5000)
    public void checkForSkip() {

    }
}
