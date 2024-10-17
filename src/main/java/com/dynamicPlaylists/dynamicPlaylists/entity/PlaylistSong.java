package com.dynamicPlaylists.dynamicPlaylists.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //each playlist song belongs to a playlist
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne //each playlist song belongs to a song
    @JoinColumn(name = "song_id")
    private Song song;

    private int tally = 0;
}
