package de.lamali.musicquizbot.listener;

import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.MusicquizBot;
import de.lamali.musicquizbot.YoutubeRequest;
import de.lamali.musicquizbot.games.Song;
import de.lamali.musicquizbot.games.SongQuiz;
import de.lamali.musicquizbot.sound.AudioQuizManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class CommandListener extends ListenerAdapter {


	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		MusicquizBot.INSTANCE.getCmdMan().performGuildCommand(event.getName(), event.getInteraction());
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		String id = event.getButton().getId();
		if (id == null) return;
		Member m = event.getMember();
		String[] cmd = id.split("_");

		if (cmd.length == 2){
			String hash = cmd[0];
			String command = cmd[1];
			SongQuiz game = SongQuiz.getGame(hash);
			if (game == null) {
				event.reply("game with " + hash + " not found!").complete().deleteOriginal().queueAfter(3, TimeUnit.SECONDS);
			} else if (command.equals("join")) {
				game.join(m, event.getInteraction());
			} else if(game.host.equals(m)){
				switch (command) {
					case "start":
						game.startRequesting(event.getInteraction());
						break;
					case "guess":
						game.startGuessing(event.getInteraction());
						break;
					case "remove":
						game.end(event.getInteraction());
						break;
					case "skip":
						game.skip(event.getInteraction());
						break;
					case "reload":
						game.reload(event.getInteraction());
						break;
				}
			} else {
				event.reply("only the host " + game.host.getAsMention() + " can do this action!").complete().deleteOriginal().queueAfter(3, TimeUnit.SECONDS);
			}
		} else if (cmd.length == 3){
			String hash = cmd[0];
			String command = cmd[1];
			String link = cmd[2];
			SongQuiz game = SongQuiz.getGame(hash);
			if (game != null && game.requesting && command.equals("request")) {
				Song song = new Song(event.getUser(), link);
				game.playlist.addSong(song);
				game.playerStats.addSong(event.getUser());
				game.control.updatePlayerStats(game.playerStats);
				event.reply("song "+link+" succesfully added!").complete().deleteOriginal().queueAfter(10, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		event.getGuild().upsertCommand("quiz","start quiz").queue();
		event.getGuild().upsertCommand("help","start quiz").queue();


	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		//YoutubeRequest.getResult(message);
		User user = event.getAuthor();
		SongQuiz activeGame = SongQuiz.getGameFromUser(user);
		if (event.isFromType(ChannelType.TEXT)) {
			TextChannel channel = event.getChannel().asTextChannel();
			if (!event.getAuthor().isBot() && activeGame != null && activeGame.guessing
					&& activeGame.channel == channel) {
				String id = YoutubeRequest.getResult(message);
				String link = "https://www.youtube.com/watch?v=" +id;
				activeGame.guessCurrentSong(user, link);
			}
		}

		if (event.isFromType(ChannelType.PRIVATE)) {
			if (activeGame != null && activeGame.requesting) {
				String link = new Song(message, user).getLink();
				event.getChannel().sendMessage("This song: " + link + " ?")
						.addActionRow(Button.primary(activeGame.hash+"_request_"+link,"request"))
						.queue();
			}
		}
	}



}
