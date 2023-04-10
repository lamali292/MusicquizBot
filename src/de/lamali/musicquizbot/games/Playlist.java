package de.lamali.musicquizbot.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist {
    public int currentSongIndex = -1;
    List<Song> songs = new ArrayList<>();
    public Playlist() {

    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(songs);
    }

    public Song getNext(){
        currentSongIndex++;
        if (currentSongIndex >= songs.size()) {
            return null;
        }
        return songs.get(currentSongIndex);
    }

    public Song getPlaying(){
        return songs.get(currentSongIndex);
    }

    public String getCount() {
        return (currentSongIndex+1) + "/" + songs.size();
    }


}
