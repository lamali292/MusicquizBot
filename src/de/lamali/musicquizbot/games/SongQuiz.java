package de.lamali.musicquizbot.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.sound.AudioQuizManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public class SongQuiz {

	public static HashMap<String, SongQuiz> games = new HashMap<>();
	public Playlist playlist;
	public List<Member> players = new ArrayList<Member>();
	public String hash;
	public TextChannel channel;
	public AudioChannel audio;
	public PlayerStats playerStats;
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
	public int guessTime = 120;
	public Member host;
	public boolean requesting = false;
	public boolean guessing = false;

	public ControllerMessage control;

	public SongQuiz(TextChannel channel, AudioChannel audio, SlashCommandInteraction startInteraction) {
		this.channel = channel;
		this.audio = audio;
		this.host = startInteraction.getMember();
		playerStats = new PlayerStats();
		genHash();

		control = new ControllerMessage(channel, hash, startInteraction);
		startInteraction.reply("Loading...").complete().deleteOriginal().queue();
	}

	public void genHash() {
		hash = "#"+(int)(Math.random()*10000);
		while(SongQuiz.getGame(hash)!=null) {
			hash = "#"+(int)(Math.random()*10000);
		}
		games.put(hash, this);
	}

	public void join(Member m, ButtonInteraction joinInteraction ) {
		if (!players.contains(m)) {
			playerStats.addPlayer(m.getUser());
			players.add(m);
		}
		control.updatePlayerStats(playerStats);
		joinInteraction.reply("Loading...").complete().deleteOriginal().queue();
	}

	public void startRequesting(ButtonInteraction interaction) {
		playlist = new Playlist();
		if (players.size() == 0) {
			control.setDescription("Closed Lobby with hash " + hash + ". Not enough players!");
			end(interaction);
			return;
		}
		requesting = true;
		for (Member player : players) {
			playerStats.addPlayer(player.getUser());
			sendMessage(player.getUser(), "Request song names here!");
		}
		control.setMessageAction(control.getBuilder()
						.setDescription("Request songs now! (private chat)")
						.build(),
				Button.primary(hash + "_guess", "guess"),
				Button.danger(hash+"_remove","end"));

		interaction.reply("Loading...").complete().deleteOriginal().queue();
	}

	public void startGuessing(ButtonInteraction interaction) {
		requesting = false;
		if (playlist.isEmpty()) {
			control.setDescription("Closed Lobby with hash "+hash+". Not enough Songs!");
			end(interaction);
			return;
		}
		playlist.shuffle();
		interaction.reply("Loading...").complete().deleteOriginal().queue();
		guessing = true;
		nextRound();
	}

	public void nextRound() {

		Song song = playlist.getNext();
		if (song == null) {
			AudioQuizManager.endSong(audio);
			guessing = false;
		} else {
			control.setMessageAction(control.getBuilder().setDescription("next song requested by " + song.getUser().getAsMention() + ". You have " + guessTime + "s to get the song!").build(),
					Button.primary(hash + "_reload", "reload"),
					Button.primary(hash + "_skip", "skip"),
					Button.danger(hash+"_remove","end"));
			AudioQuizManager.playSong(song, audio);
		}
		executor.schedule(() -> {
			if (song != null && song == playlist.getPlaying()) {
				control.setDescription("Time is up:"+song.getLink());
				control.updatePlayerStats(playerStats);
				nextRound();
			}
		}, guessTime, TimeUnit.SECONDS);
	}

	public void guessCurrentSong(User user, String link) {
		Song song = playlist.getPlaying();
		if (song.getUser().getId().equals(user.getId()) && players.size() > 1) {
			channel.sendMessage(user.getAsMention()+" please dont guess your own songs!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
			return;
		}
		if (song.isLink(link)) {
			control.setDescription(user.getAsMention()+" guessed "+link+" correctly");
			channel.sendMessage("This was the song: "+ playlist.getPlaying().getLink()).queue();
			playerStats.addPoint(user);
			control.updatePlayerStats(playerStats);
			nextRound();

		}
	}

	public String getAllPlayers() {
		StringBuilder s = new StringBuilder();
		for (Member e : players) {
			s.append(e.getAsMention());
		}
		return s.toString();
	}


	public void end(ButtonInteraction interaction) {
		if (interaction != null) {
			interaction.reply("Song Quiz Game " + hash + " ended!").complete().deleteOriginal().queueAfter(5,TimeUnit.SECONDS);
		}
		AudioQuizManager.endSong(audio);
		games.remove(hash);
	}


	public void sendMessage(User user, String content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}

	public static SongQuiz getGame(String hash) {
		return games.get(hash);
	}

	public static SongQuiz getGameFromUser(User user) {
		for (SongQuiz game : games.values()) {
			for (Member mem : game.players) {
				if (mem.getUser() == user) {
					return game;
				}
			}
		}
		return null;
	}

	public void skip(ButtonInteraction interaction) {
		nextRound();
		control.setDescription("Skipped: "+playlist.getPlaying().getLink());
		channel.sendMessage("This was the song: "+ playlist.getPlaying().getLink()).queue();
		control.updatePlayerStats(playerStats);
	}

	public void reload(ButtonInteraction interaction) {
		AudioQuizManager.playSong(playlist.getPlaying(),audio);
		control.setDescription("Song reloaded!");
	}
}
