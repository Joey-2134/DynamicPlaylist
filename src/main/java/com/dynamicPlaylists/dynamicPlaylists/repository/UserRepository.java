package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findAllByIsPlaying(boolean b);
    User findUserById(String userId);

    User  findUserByAccessToken(String accessToken);
}
