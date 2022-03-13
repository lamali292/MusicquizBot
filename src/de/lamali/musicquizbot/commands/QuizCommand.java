package de.lamali.musicquizbot.commands;

import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.commands.types.ServerCommand;
import de.lamali.musicquizbot.games.SongQuiz;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class QuizCommand implements ServerCommand {
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] args = message.getContentDisplay().split(" ");
		GuildVoiceState vState = m.getVoiceState();
		if (vState == null) {
			channel.sendMessage("Voice Channel not found! Join Audio Channel!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
			message.delete().queue();
			return;
		}
		AudioChannel vc = vState.getChannel();
		if (vc == null) {
			channel.sendMessage("Voice Channel not found! Join Audio Channel!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
			message.delete().queue();
			return;
		}
		if (args.length == 2) {
			String command = args[1];
			if (command.equalsIgnoreCase("start")) {
				String hash = "#"+(int)(Math.random()*10000);
				while(SongQuiz.getGame(hash)!=null) {
					hash = "#"+(int)(Math.random()*10000);
				}
				new SongQuiz(hash, channel, vc);
			}
			message.delete().queue();
		}else if (args.length == 3) {
			String command = args[1];
			String hash = args[2];
			SongQuiz game = SongQuiz.getGame(hash);
			if (command.equalsIgnoreCase("start")) {
				if (game == null) {
					SongQuiz quiz = new SongQuiz(hash, channel, vc);
					quiz.lobbyTime = Integer.parseInt(args[3]);
					quiz.lobbyTime = Integer.parseInt(args[4]);
				} else {
					channel.sendMessage("Already started Lobby with hash: " + hash).complete().delete().queueAfter(3,
							TimeUnit.SECONDS);
				}

			} else if (game == null) {
				channel.sendMessage("Game not found").complete().delete().queueAfter(3, TimeUnit.SECONDS);
			} else {
				if (command.equalsIgnoreCase("join")) {
					game.join(m);
				} else if (command.equalsIgnoreCase("end")) {
					game.end();
				}
			}
			message.delete().queue();

		}else if (args.length == 5) {
			String command = args[1];
			if (command.equalsIgnoreCase("start")) {
				String hash = "#"+(int)(Math.random()*10000);
				while(SongQuiz.getGame(hash)!=null) {
					hash = "#"+(int)(Math.random()*10000);
				}
				new SongQuiz(hash, channel, vc, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			}
			message.delete().queue();

		}

	}

	public void sendMessage(User user, String content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}

}
