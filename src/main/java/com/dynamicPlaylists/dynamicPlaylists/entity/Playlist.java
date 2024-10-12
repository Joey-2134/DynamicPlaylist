package com.dynamicPlaylists.dynamicPlaylists.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Playlist {
    @Id
    private String id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id") //each playlist belongs to a user
    private User user;

    @ManyToMany //each playlist can have multiple songs
    @JoinTable( //join table for playlist and songs
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    private List<Song> songs = new ArrayList<>();

}
