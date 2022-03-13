package de.lamali.musicquizbot.sound;

import java.util.concurrent.ConcurrentHashMap;

import de.lamali.musicquizbot.MusicquizBot;

public class PlayerManager {
	public ConcurrentHashMap<Long, SoundController> controller;
	
	public PlayerManager() {
		this.controller = new ConcurrentHashMap<Long, SoundController>();
	}
	
	public SoundController getController(long guildId) {
		SoundController sc = null;
		if(this.controller.containsKey(guildId)) {
			sc = this.controller.get(guildId);
		}else {
			sc = new SoundController(MusicquizBot.INSTANCE.shardMan.getGuildById(guildId));
			
			this.controller.put(guildId, sc);
		}
		
		return sc;
	}
}
