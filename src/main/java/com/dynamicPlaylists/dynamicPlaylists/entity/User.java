package com.dynamicPlaylists.dynamicPlaylists.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String username;

    @Column(length = 1024)
    private String accessToken;

    @Column(length = 1024)
    private String refreshToken;

    private String scope;
    private long expiresIn; // The time period (in seconds) for which the access token is valid.
    private int skipThreshold;
    private int cooldownDays;

    public User(String id, String username, int skipThreshold, int cooldownDays) {
        super();
        this.id = id;
        this.username = username;
        this.skipThreshold = skipThreshold;
        this.cooldownDays = cooldownDays;
    }

    public User(String id, String username) {
        super();
        this.id = id;
        this.username = username;
    }


}
