package de.lamali.musicquizbot.listener;

import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.MusicquizBot;
import de.lamali.musicquizbot.YoutubeRequest;
import de.lamali.musicquizbot.games.Song;
import de.lamali.musicquizbot.games.SongQuiz;
import de.lamali.musicquizbot.games.SongQuiz.GameState;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		//YoutubeRequest.getResult(message);
		User user = event.getAuthor();
		SongQuiz activeGame = SongQuiz.getGameFromUser(user);
		if (event.isFromType(ChannelType.TEXT)) {
			TextChannel channel = event.getTextChannel();
			if (message.startsWith("!")) {
				String[] args = message.substring(1).split(" ");
				if (args.length > 0) {
					if (!MusicquizBot.INSTANCE.getCmdMan().performServerCommand(args[0], event.getMember(), channel,
							event.getMessage())) {
						channel.sendMessage("Unbekanntes Commando!").queue();
					}
				}
			} else if (!event.getAuthor().isBot() && activeGame != null && activeGame.state == GameState.GUESSING
					&& activeGame.channel == event.getTextChannel()) {
				String id = YoutubeRequest.getResult(message);
				String link = "https://www.youtube.com/watch?v=" +id;
				activeGame.guessCurrentSong(user, link);
			}
		}

		if (event.isFromType(ChannelType.PRIVATE)) {
			PrivateChannel channel = event.getPrivateChannel();
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
				reMes.addReaction("U+1F44D").queue();
				activeGame.requests.put(reMes.getId(), song);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		User user = event.getUser();
		SongQuiz activeGame = SongQuiz.getGameFromUser(user);
		if (event.isFromType(ChannelType.PRIVATE) && !user.isBot()) {
			if (activeGame != null && activeGame.state == GameState.REQUESTING
					&& event.getReactionEmote().toString().equals("RE:U+1f44d")) {
				Song song = activeGame.requests.get(event.getMessageId());
				activeGame.addSong(song);
				event.getChannel().purgeMessagesById(event.getReaction().getMessageId());
				event.getChannel().sendMessage("song "+song.getLink()+" succesfully added!").complete().delete().queueAfter(10, TimeUnit.SECONDS);
			}
		} 
		if (event.isFromType(ChannelType.TEXT) && !user.isBot()) {
			if(activeGame != null && activeGame.state == GameState.GUESSING
				&& event.getReactionEmote().toString().equals("RE:U+1f504")) {
				activeGame.playSong(activeGame.songs.get(activeGame.currentSongIndex));
			}
			event.getReaction().removeReaction(user).queue();
		}
	}
	

}
