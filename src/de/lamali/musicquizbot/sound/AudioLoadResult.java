package de.lamali.musicquizbot.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadResult implements AudioLoadResultHandler {
	private final String uri;
	private final SoundController controller;
	public AudioLoadResult(SoundController controller, String uri) {
		this.uri = uri;
		this.controller = controller;
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		controller.getPlayer().playTrack(track);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		if(playlist.getTracks().size()>0){
			controller.getPlayer().playTrack(playlist.getTracks().get(0));
		}
	}

	@Override
	public void noMatches() {
		System.out.println("no match: "+uri);
	}

	@Override
	public void loadFailed(FriendlyException e) {
		e.printStackTrace();
	}

}
