package de.lamali.musicquizbot.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.MusicquizBot;
import de.lamali.musicquizbot.YoutubeRequest;
import de.lamali.musicquizbot.games.Song;
import de.lamali.musicquizbot.games.SongQuiz;
import de.lamali.musicquizbot.games.SongQuiz.GameState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class CommandListener extends ListenerAdapter {


	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		String command = event.getName();
		if (command.equals("qstart")) {
			event.reply("hi");
		}
		System.out.println(command);
		super.onSlashCommandInteraction(event);
	}

	@Override
	public void onGuildReady(GuildReadyEvent event) {
		List<CommandData> commandData = new ArrayList<>();
		commandData.add(Commands.slash("qstart","start quiz"));
		event.getGuild().updateCommands().addCommands(commandData).queue();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		//YoutubeRequest.getResult(message);
		User user = event.getAuthor();
		SongQuiz activeGame = SongQuiz.getGameFromUser(user);
		if (event.isFromType(ChannelType.TEXT)) {
			TextChannel channel = event.getChannel().asTextChannel();
			if (message.startsWith("!")) {
				String[] args = message.substring(1).split(" ");
				if (args.length > 0) {
					if (!MusicquizBot.INSTANCE.getCmdMan().performServerCommand(args[0], event.getMember(), channel,
							event.getMessage())) {
						channel.sendMessage("Unbekanntes Commando!").queue();
					}
				}
			} else if (!event.getAuthor().isBot() && activeGame != null && activeGame.state == GameState.GUESSING
					&& activeGame.channel == event.getChannel()) {
				String id = YoutubeRequest.getResult(message);
				String link = "https://www.youtube.com/watch?v=" +id;
				activeGame.guessCurrentSong(user, link);
			}
		}

		if (event.isFromType(ChannelType.PRIVATE)) {
			PrivateChannel channel = event.getChannel().asPrivateChannel();
			if (message.startsWith("!")) {
				String[] args = message.substring(1).split(" ");
				if (args.length > 0) {
					if (!MusicquizBot.INSTANCE.getCmdMan().performPrivateCommand(args[0], event.getAuthor(), channel,
							event.getMessage())) {
						channel.sendMessage("Unbekanntes Commando!").queue();
					}
				}
			} else if (activeGame != null && activeGame.state == GameState.REQUESTING) {
				Song song = new Song(message, user);
				Message reMes = event.getChannel().sendMessage("This song: " + song.getLink() + " ?").complete();
				reMes.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
				activeGame.requests.put(reMes.getId(), song);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		User user = event.getUser();
		if (user == null) return;
		SongQuiz activeGame = SongQuiz.getGameFromUser(user);
		if (event.isFromType(ChannelType.PRIVATE) && !user.isBot()) {

			if (activeGame != null && activeGame.state == GameState.REQUESTING
					&& event.getReaction().getEmoji().asUnicode().getAsCodepoints().equals("U+1f44d")) {
				Song song = activeGame.requests.get(event.getMessageId());
				activeGame.addSong(song);
				event.getChannel().purgeMessagesById(event.getReaction().getMessageId());
				event.getChannel().sendMessage("song "+song.getLink()+" succesfully added!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
		}
		if (event.isFromType(ChannelType.TEXT) && !user.isBot()) {
			if(activeGame != null && activeGame.state == GameState.GUESSING
					&& event.getReaction().getEmoji().asUnicode().getAsCodepoints().equals("U+1f504")) {
				activeGame.playSong(activeGame.songs.get(activeGame.currentSongIndex));
			}
			event.getReaction().removeReaction(user).queue();
		}
	}


}
