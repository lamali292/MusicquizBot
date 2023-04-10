package de.lamali.musicquizbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import de.lamali.musicquizbot.listener.CommandListener;
import de.lamali.musicquizbot.sound.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class MusicquizBot {
	public static MusicquizBot INSTANCE;

	public JDA jda;
	private final CommandManager cmdMan;
	public AudioPlayerManager audioPlayerManager;
	public PlayerManager playerManager;

	public static void main(String[] args) {
		try {
			Constants.JDA_TOKEN = args[0];
			new MusicquizBot();
		} catch (LoginException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public MusicquizBot() throws LoginException, IllegalArgumentException {
		INSTANCE = this;
		ArrayList<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_VOICE_STATES);
		intents.add(GatewayIntent.GUILD_PRESENCES);
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.DIRECT_MESSAGES);
		intents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
		intents.add(GatewayIntent.SCHEDULED_EVENTS);
		intents.add(GatewayIntent.MESSAGE_CONTENT);

		JDABuilder builder = JDABuilder.createDefault(Constants.JDA_TOKEN, intents);

		builder.setActivity(Activity.watching("nach Spielern"));
		builder.setStatus(OnlineStatus.ONLINE);

		this.audioPlayerManager = new DefaultAudioPlayerManager();
		this.playerManager = new PlayerManager();

		AudioSourceManagers.registerLocalSource(audioPlayerManager);
		AudioSourceManagers.registerRemoteSources(audioPlayerManager);

		this.cmdMan = new CommandManager();

		builder.addEventListeners(new CommandListener());

		jda = builder.build();
		System.out.println("Bot online");

		shutdown();

	}


	public void shutdown() {
		new Thread(() -> {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				while ((line = reader.readLine()) != null) {
					if (line.equalsIgnoreCase("exit")) {
						if (jda != null) {
							jda.shutdown();
							System.out.println("Bot offline");
						}

						reader.close();
					} else {
						System.out.println("Use 'exit' to shutdown");
					}
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}).start();

	}

	public CommandManager getCmdMan() {
		return cmdMan;
	}
}
