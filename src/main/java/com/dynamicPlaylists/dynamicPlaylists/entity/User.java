package com.dynamicPlaylists.dynamicPlaylists.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

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
    private boolean isPlaying;

    private String lastTrackId;
    private int lastTrackLength;
    private float lastTrackProgress;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) //each user can have multiple playlists
    private List<Playlist> playlists = new ArrayList<>();

    public User(String id, String username) {
        super();
        this.id = id;
        this.username = username;
        this.skipThreshold = 2;
        this.lastTrackId = "2up3OPMp9Tb4dAKM2erWXQ";
    }

}
