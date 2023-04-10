package de.lamali.musicquizbot.games;

import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;

public class PlayerStats {
    public HashMap<User, Integer> points = new HashMap<>();
    public HashMap<User, Integer> songCount = new HashMap<>();
    public void addPlayer(User user) {
        points.put(user, 0);
        songCount.put(user, 0);
    }

    public void addPoint(User user) {
        if (!points.containsKey(user)) {
            points.put(user, 0);
        }
        int point = points.get(user);
        points.replace(user, point + 1);
    }

    public void addSong(User user) {
        if (!songCount.containsKey(user)) {
            songCount.put(user, 0);
        }
        int point = songCount.get(user);
        songCount.replace(user, point + 1);
    }
}
