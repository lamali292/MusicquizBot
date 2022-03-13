package de.lamali.musicquizbot.games;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import de.lamali.musicquizbot.MusicquizBot;
import de.lamali.musicquizbot.sound.AudioLoadResult;
import de.lamali.musicquizbot.sound.SoundController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.managers.AudioManager;

public class SongQuiz {
	
	public static HashMap<String, SongQuiz> games = new HashMap<>();
	public HashMap<String, Song> requests = new HashMap<>();
	public HashMap<User, Integer> leaderbord = new HashMap<>();
	public List<Song> songs = new ArrayList<Song>();
	public HashMap<Song, Boolean> guessed = new HashMap<>();
	public List<Member> players = new ArrayList<Member>();
	public String hash;
	public GameState state = GameState.LOBBY;
	public TextChannel channel;
	public AudioChannel audio;
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	public int lobbyTime = 30;
	public int requestTime = 120;
	public int guessTime = 120;
	public int currentSongIndex = 0;
	
	public enum GameState {
		LOBBY, REQUESTING, BETWEEN, GUESSING, ENDED
	}

	
	public SongQuiz(String hash, TextChannel channel, AudioChannel audio) {
		this.channel = channel;
		this.audio = audio;
		this.hash = hash;
		if (!games.containsKey(hash)) {
			games.put(hash, this);
			startLobby();
		} else {
			channel.sendMessage("Hash already used!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
		}
	}
	
	public SongQuiz(String hash, TextChannel channel, AudioChannel audio, int lobbyTime, int requestTime, int guessTime) {
		this.lobbyTime = lobbyTime;
		this.requestTime = requestTime;
		this.guessTime = guessTime;
		this.channel = channel;
		this.audio = audio;
		this.hash = hash;
		if (!games.containsKey(hash)) {
			games.put(hash, this);
			startLobby();
		} else {
			channel.sendMessage("Hash already used!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
		}
	}
	
	public void startLobby() {
		channel.sendMessage("Song Quiz lobby started. Join with **!quiz join "+hash+"** in the next "+lobbyTime+" Seconds.").complete().delete().queueAfter(lobbyTime, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				channel.sendMessage("Game "+hash+" starts in "+(lobbyTime-lobbyTime/2)+" Seconds!").complete().delete().queueAfter(lobbyTime-lobbyTime/2, TimeUnit.SECONDS);
			}
		}, lobbyTime/2, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				channel.sendMessage("Game "+hash+" starts in "+lobbyTime/10+" Seconds!").complete().delete().queueAfter(lobbyTime/10, TimeUnit.SECONDS);
			}
		}, lobbyTime-lobbyTime/10, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (players.size() > 0) {
					startRequesting();
				} else {
					games.remove(hash);
					channel.sendMessage("Closed Lobby with hash "+hash+". Not enough players!").complete().delete().queueAfter(20, TimeUnit.SECONDS);
				}
			}
		}, lobbyTime, TimeUnit.SECONDS);
	}
	
	public static SongQuiz getGame(String hash) {
		return games.get(hash);
	}
	
	public static boolean isGuessingActive(TextChannel channel) {
		for (SongQuiz game : games.values()) {
			if (game.channel == channel) {
				return game.state == GameState.GUESSING;
			}
		}
		return false;
	}
	
	public static SongQuiz getGameFromUser(User author) {
		for (SongQuiz game : games.values()) {
			for (Member mem : game.players) {
				if (mem.getUser() == author) {
					return game;
				}
			}
		}
		return null;
	}
	
	public void startRequesting() {
		state = GameState.REQUESTING;
		String msg = "";
		for (Member player : players) {
			msg += " "+player.getAsMention();
			leaderbord.put(player.getUser(), 0);
			sendMessage(player.getUser(), "Request song names here!");
		}
		channel.sendMessage("Started Game with" + msg + ".  Use **!quiz end "+hash+"** to end the game.").queue();
		channel.sendMessage("Request Songs now! (private message) time: "+requestTime+"s").complete().delete().queueAfter(requestTime, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (songs.size() > 0) {
					startGuessing();
				} else {
					games.remove(hash);
					channel.sendMessage("Closed Lobby with hash "+hash+". Not enough Songs!").complete().delete().queueAfter(20, TimeUnit.SECONDS);
				}
			}


		}, requestTime, TimeUnit.SECONDS);
	}
	
	public void startGuessing() {
		Collections.shuffle(songs);
		for (Song song : songs) {
			guessed.put(song, false);
		}
		state = GameState.BETWEEN;
		startNextRound();
	}
	
	public void startNextRound() {
		state = GameState.GUESSING;
		Song song = songs.get(currentSongIndex);
		int songindex = currentSongIndex;
		channel.sendMessage("next song requested by "+song.getUser().getAsMention()+". You have "+guessTime+"s to get the song!").queue();
		playSong(song);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (!guessed.get(song)) {
					channel.sendMessage("Round ends in "+(guessTime-guessTime/2)+" Seconds!").complete().delete().queueAfter(guessTime-guessTime/2, TimeUnit.SECONDS);
				}
			}
		}, guessTime/2, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (!guessed.get(song)) {
					channel.sendMessage("Round ends in "+guessTime/10+" Seconds!").complete().delete().queueAfter(guessTime/10, TimeUnit.SECONDS);
				}
			}
		}, guessTime-guessTime/10, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				state = GameState.BETWEEN;
				Song song = songs.get(songindex);
				if (!guessed.get(song)) {
					guessed.replace(song, true);
					channel.sendMessage("Time Is Up: Song was "+song.getLink()+" !").queue();
					showPoints();
					currentSongIndex++;
					if (currentSongIndex < songs.size()) {
						startNextRound();
					} else {
						end();
					}
				}
			}
		}, guessTime, TimeUnit.SECONDS);
	}
	
	public void guessCurrentSong(User user, String link) {
		Song song = songs.get(currentSongIndex);
		if (song.getUser().getId().equals(user.getId()) && players.size() > 1) {
			channel.sendMessage(user.getAsMention()+" please dont guess your own songs!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			return;
		}
		if (song.isLink(link)) {
			guessed.replace(song, true);
			channel.sendMessage(user.getAsMention()+" guessed "+link+" correctly").queue();
			state = GameState.BETWEEN;
			addPoint(user);
			showPoints();
			currentSongIndex++;
			if (currentSongIndex < songs.size()) {
				startNextRound();
			} else {
				end();
			}
			
		}
	}
	
	public void addPoint(User user) {
		if (!leaderbord.keySet().contains(user)) {
			leaderbord.put(user, 0);
		}
		int points = leaderbord.get(user);
		leaderbord.replace(user, points + 1);
	}
	
	public void showPoints() {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Color.cyan);
		embed.setTimestamp(Instant.now());
		embed.setTitle("Leaderboard:");
		
		String users = "";
		String points = "";
		for (User user : leaderbord.keySet()) {
			users += user.getAsMention() + ":\n";
			points += leaderbord.get(user) + "\n";
		}
		embed.addField("Round "+(currentSongIndex+1)+"/"+songs.size(), users, true);
		embed.addField("", points, true);

		embed.addBlankField(true);
		channel.sendMessageEmbeds(embed.build()).queue();
	}
	
	public void playSong(Song song) {
		audio = players.get(0).getVoiceState().getChannel();
		SoundController controller = MusicquizBot.INSTANCE.playerManager.getController(audio.getGuild().getIdLong());
		AudioPlayerManager apm = MusicquizBot.INSTANCE.audioPlayerManager;
		AudioManager audioManager = audio.getGuild().getAudioManager();
		audioManager.openAudioConnection(audio);
		String url = song.getLink();
		apm.loadItem(url, new AudioLoadResult(controller, url));
	}
	
	public void endSong() {
		AudioManager audioManager = audio.getGuild().getAudioManager();
		audioManager.closeAudioConnection();
	}
	
	public void join(Member m) {
		if (state == GameState.LOBBY) {
			if (!players.contains(m)) {
				players.add(m);
				channel.sendMessage(m.getAsMention() + " joined the game").complete().delete().queueAfter(1, TimeUnit.MINUTES);
				//sendMessage(m.getUser(), "You joined Cluedo with game hash "+hash+"");
			}
		} else {
			channel.sendMessage("Already ingame!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
		}
	}
	
	public void end() {
		channel.sendMessage("Song Quiz Game "+hash+" ended!").queue();
		endSong();
		state = GameState.ENDED;
		games.remove(hash);
	}
	
	public void sendMessage(User user, String content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}
	
	public void sendMessageEmbeds(User user, MessageEmbed content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(content)).queue();
	}
	
	public void sendMessage(User user, Message content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}

	public void addSong(Song song) {
		songs.add(song);
	}

}
