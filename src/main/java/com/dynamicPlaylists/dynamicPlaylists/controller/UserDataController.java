package com.dynamicPlaylists.dynamicPlaylists.controller;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import com.dynamicPlaylists.dynamicPlaylists.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserDataController {

    private final UserRepository userRepository;

    public UserDataController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/updateUserSkipThreshold")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateUserSkipThreshold(@RequestBody Map<String, String> body, @RequestParam int skipThreshold) {
        String encryptedAccessToken = body.get("encryptedAccessToken");
        User user = userRepository.findUserByAccessToken(encryptedAccessToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with the provided access token.");
        }
        user.setSkipThreshold(skipThreshold);
        userRepository.save(user);
        return ResponseEntity.ok("Skip threshold updated successfully.");
    }


    @PutMapping("/updateUserCooldownDays")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> updateUserCooldownDays(@RequestBody Map<String, String> body, @RequestParam int cooldownDays) {
        String encryptedAccessToken = body.get("encryptedAccessToken");
        User user = userRepository.findUserByAccessToken(encryptedAccessToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with the provided access token.");
        }
        user.setCooldownDays(cooldownDays);
        userRepository.save(user); // Ensure you save the changes
        return ResponseEntity.ok("Cooldown days updated successfully.");
    }


}
