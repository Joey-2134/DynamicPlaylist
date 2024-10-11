package com.dynamicPlaylists.dynamicPlaylists.repository;

import com.dynamicPlaylists.dynamicPlaylists.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
