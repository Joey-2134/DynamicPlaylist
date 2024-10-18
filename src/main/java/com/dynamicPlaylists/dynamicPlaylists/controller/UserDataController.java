package com.dynamicPlaylists.dynamicPlaylists.controller;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserDataController {

    private final UserRepository userRepository;

    public UserDataController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/updateUserSkipThreshold")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserSkipThreshold(@RequestParam String encryptedAccessToken, @RequestParam int skipThreshold) {
        User user = userRepository.findUserByAccessToken(encryptedAccessToken);
        user.setSkipThreshold(skipThreshold);
    }

    @PutMapping("/updateUserCooldownDays")
    @ResponseStatus(HttpStatus.OK)
    public void updateUserCooldownDays(@RequestParam String encryptedAccessToken, @RequestParam int cooldownDays) {
        User user = userRepository.findUserByAccessToken(encryptedAccessToken);
        user.setCooldownDays(cooldownDays);
    }
}
