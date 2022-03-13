package de.lamali.musicquizbot.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import de.lamali.musicquizbot.MusicquizBot;
import net.dv8tion.jda.api.entities.Guild;

public class SoundController {
	private Guild guild;
	private AudioPlayer player;
	
	public SoundController(Guild guild) {
		this.guild = guild;
		this.player = MusicquizBot.INSTANCE.audioPlayerManager.createPlayer();
		
		this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
		this.player.setVolume(5);
	}
	
	public Guild getGuild() {
		return guild;
	}
	
	public AudioPlayer getPlayer() {
		return player;
	}
}
