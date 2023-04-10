package de.lamali.musicquizbot.commands;

import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.commands.types.ServerCommand;
import de.lamali.musicquizbot.games.SongQuiz;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class QuizCommand implements ServerCommand {
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		String[] args = message.getContentDisplay().split(" ");
		String command = args[1];
		if (command.equalsIgnoreCase("start")) {
			GuildVoiceState vState = m.getVoiceState();
			if (vState == null) {
				channel.sendMessage("Voice Channel not found! Join Audio Channel!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
				return;
			}
			AudioChannel vc = vState.getChannel();
			if (vc == null) {
				channel.sendMessage("Voice Channel not found! Join Audio Channel!").complete().delete().queueAfter(3, TimeUnit.SECONDS);
				return;
			}
			if (args.length == 2) {
				String hash = "#"+(int)(Math.random()*10000);
				while(SongQuiz.getGame(hash)!=null) {
					hash = "#"+(int)(Math.random()*10000);
				}
				new SongQuiz(hash, channel, vc);
			} else if (args.length == 3) {
				String hash = args[2];
				SongQuiz game = SongQuiz.getGame(hash);
				if (game == null) {
					new SongQuiz(hash, channel, vc);
				} else {
					channel.sendMessage("Already started Lobby with hash: " + hash).complete().delete().queueAfter(3,
							TimeUnit.SECONDS);
				}
			}  else if (args.length == 5) {
				String hash = "#"+(int)(Math.random()*10000);
				while(SongQuiz.getGame(hash)!=null) {
					hash = "#"+(int)(Math.random()*10000);
				}
				new SongQuiz(hash, channel, vc, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
			}
		} else if (command.equalsIgnoreCase("join")) {
			String hash = args[2];
			SongQuiz game = SongQuiz.getGame(hash);
			if (game == null) {
				channel.sendMessage("game with "+hash+" not found!").complete().delete().queueAfter(3,TimeUnit.SECONDS);
			} else {
				game.join(m);
			}

		} else if (command.equalsIgnoreCase("end")) {
			String hash = args[2];
			SongQuiz game = SongQuiz.getGame(hash);
			if (game == null) {
				channel.sendMessage("game with "+hash+" not found!").complete().delete().queueAfter(3,TimeUnit.SECONDS);
			} else {
				game.end();
			}
		}
		message.delete().queue();

	}

	public void sendMessage(User user, String content) {
		user.openPrivateChannel().flatMap(channel -> channel.sendMessage(content)).queue();
	}

}
