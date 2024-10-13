package com.dynamicPlaylists.dynamicPlaylists.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Song {

    @Id
    private String id;

    private String name;
    private String artist; //todo potentially make this a list of artist or list of artist entities

    @ManyToMany(mappedBy = "songs")
    private List<Playlist> playlists = new ArrayList<>();

    public Song(String id, String name, String artist) {
        super();
        this.id = id;
        this.name = name;
        this.artist = artist;
    }
}
