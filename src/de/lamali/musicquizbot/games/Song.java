package de.lamali.musicquizbot.games;

import de.lamali.musicquizbot.YoutubeRequest;
import net.dv8tion.jda.api.entities.User;

public class Song {
	private User user;
	private String link;

	public Song(User user, String link) {
		this.user = user;
		this.link = link;
	}
	
	public Song (String keyword, User user) {
		String id = YoutubeRequest.getResult(keyword);
		this.link = "https://www.youtube.com/watch?v="+id;
		this.user = user;
	}

	public User getUser() {
		return user;
	}
			
	public String getLink() {
		return link;
	}
	
	public boolean isLink(String s) {
		return link.equals(s);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Song) {
			Song song = (Song) obj;
			return link.equals(song.link);
		}
		return false;
	}
}
