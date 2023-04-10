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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SongQuiz {

	public static HashMap<String, SongQuiz> games = new HashMap<>();
	public HashMap<String, Song> requests = new HashMap<>();
	public HashMap<User, Integer> leaderbord = new HashMap<>();
	public HashMap<Song, Boolean> guessed = new HashMap<>();
	public List<Song> songs = new ArrayList<Song>();
	public List<Member> players = new ArrayList<Member>();
	public GameState state = GameState.LOBBY;
	public String hash;
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


	public SongQuiz(String hash, TextChannel channel, AudioChannel audio, SlashCommandInteraction startInteraction) {
		this.channel = channel;
		this.audio = audio;
		this.hash = hash;
		if (!games.containsKey(hash)) {
			games.put(hash, this);
			replyStart(startInteraction);
			startLobby();
		} else {
			startInteraction.reply("Hash already used!").complete().deleteOriginal().queueAfter(3,TimeUnit.SECONDS);
		}
	}

	public SongQuiz(String hash, TextChannel channel, AudioChannel audio, SlashCommandInteraction startInteraction, int lobbyTime, int requestTime, int guessTime) {
		this.lobbyTime = lobbyTime;
		this.requestTime = requestTime;
		this.guessTime = guessTime;
		this.channel = channel;
		this.audio = audio;
		this.hash = hash;
		if (!games.containsKey(hash)) {
			games.put(hash, this);
			replyStart(startInteraction);
			startLobby();
		} else {
			startInteraction.reply("Hash already used!").complete().deleteOriginal().queueAfter(3,TimeUnit.SECONDS);
		}
	}

	public void replyStart(SlashCommandInteraction startInteraction){
		startInteraction.reply("Song Quiz lobby started. Join in the next "+lobbyTime+" Seconds.")
				.addActionRow(Button.primary(hash+"_join_",Emoji.fromUnicode("U+1F44D")))
				.addActionRow(Button.primary(hash+"_remove",Emoji.fromUnicode("U+1F6AB")))
				.queue();
	}



	public void startLobby() {
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (state != GameState.ENDED) {
					channel.sendMessage("Game "+hash+" starts in "+(lobbyTime-lobbyTime/2)+" Seconds!").complete().delete().queueAfter(lobbyTime-lobbyTime/2, TimeUnit.SECONDS);
				}
			}
		}, lobbyTime/2, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (state != GameState.ENDED) {
					channel.sendMessage("Game "+hash+" starts in "+lobbyTime/10+" Seconds!").complete().delete().queueAfter(lobbyTime/10, TimeUnit.SECONDS);
				}
			}
		}, lobbyTime-lobbyTime/10, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (state != GameState.ENDED) {
					if (players.size() > 0) {
						startRequesting();
					} else {
						channel.sendMessage("Closed Lobby with hash "+hash+". Not enough players!").complete().delete().queueAfter(20, TimeUnit.SECONDS);
						end();
					}
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
		StringBuilder msg = new StringBuilder();
		for (Member player : players) {
			msg.append(" ").append(player.getAsMention());
			leaderbord.put(player.getUser(), 0);
			sendMessage(player.getUser(), "Request song names here!");
		}
		channel.sendMessage("Started Game with" + msg + ".").queue();
		channel.sendMessage("Request Songs now! (private message) time: "+requestTime+"s").complete().delete().queueAfter(requestTime, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (state != GameState.ENDED) {
					if (songs.size() > 0) {
						startGuessing();
					} else {
						channel.sendMessage("Closed Lobby with hash "+hash+". Not enough Songs!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
						end();
					}
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
		Message mess = channel.sendMessage("next song requested by "+song.getUser().getAsMention()+". You have "+guessTime+"s to get the song!").complete();
		mess.delete().queueAfter(guessTime, TimeUnit.SECONDS);
		mess.addReaction(Emoji.fromUnicode("U+1F504")).queue();
		playSong(song);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (!guessed.get(song) && state != GameState.ENDED) {
					channel.sendMessage("Round ends in "+(guessTime-guessTime/2)+" Seconds!").complete().delete().queueAfter(guessTime-guessTime/2, TimeUnit.SECONDS);
				}
			}
		}, guessTime/2, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				if (!guessed.get(song) && state != GameState.ENDED) {
					channel.sendMessage("Round ends in "+guessTime/10+" Seconds!").complete().delete().queueAfter(guessTime/10, TimeUnit.SECONDS);
				}
			}
		}, guessTime-guessTime/10, TimeUnit.SECONDS);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				Song song = songs.get(songindex);
				if (!guessed.get(song) && state != GameState.ENDED) {
					state = GameState.BETWEEN;
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
		if (!leaderbord.containsKey(user)) {
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

		StringBuilder users = new StringBuilder();
		StringBuilder points = new StringBuilder();
		for (User user : leaderbord.keySet()) {
			users.append(user.getAsMention()).append(":\n");
			points.append(leaderbord.get(user)).append("\n");
		}
		embed.addField("Round "+(currentSongIndex+1)+"/"+songs.size(), users.toString(), true);
		embed.addField("", points.toString(), true);

		embed.addBlankField(true);
		channel.sendMessageEmbeds(embed.build()).queue();
	}

	public void playSong(Song song) {
		SoundController controller = MusicquizBot.INSTANCE.playerManager.getController(audio.getGuild().getIdLong());
		AudioPlayerManager apm = MusicquizBot.INSTANCE.audioPlayerManager;
		AudioManager audioManager = audio.getGuild().getAudioManager();
		controller.getPlayer().setVolume(100);
		audioManager.openAudioConnection(audio);
		String url = song.getLink();
		apm.loadItem(url, new AudioLoadResult(controller, url));
	}

	public void endSong() {
		AudioManager audioManager = audio.getGuild().getAudioManager();
		audioManager.closeAudioConnection();
	}

	public void join(Member m, ButtonInteraction joinInteraction ) {
		if (state == GameState.LOBBY) {
			if (!players.contains(m)) {
				players.add(m);
				joinInteraction.reply(m.getAsMention() + " joined the game").complete().deleteOriginal().queueAfter(lobbyTime, TimeUnit.SECONDS);
				//sendMessage(m.getUser(), "You joined Cluedo with game hash "+hash+"");
			}
		} else if (state != GameState.ENDED) {
			channel.sendMessage("Already ingame!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
		}
	}

	public void end() {
		endSong();
		state = GameState.ENDED;
		games.remove(hash);
	}


	public void sendMessage(User user, String content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}

	public void addSong(Song song) {
		songs.add(song);
	}

}
