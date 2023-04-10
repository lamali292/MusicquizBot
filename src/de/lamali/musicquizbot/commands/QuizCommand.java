package de.lamali.musicquizbot.commands;

import java.util.concurrent.TimeUnit;

import de.lamali.musicquizbot.commands.types.GuildCommand;
import de.lamali.musicquizbot.games.SongQuiz;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class QuizCommand implements GuildCommand {
	@Override
	public void performCommand(SlashCommandInteraction interaction) {
		TextChannel channel = interaction.getChannel().asTextChannel();
		Member m = interaction.getMember();

		GuildVoiceState vState = m.getVoiceState();
		if (vState == null) {
			interaction.reply("Voice Channel not found! Join Audio Channel!").complete().deleteOriginal().queueAfter(3,TimeUnit.SECONDS);
			return;
		}
		AudioChannel vc = vState.getChannel();
		if (vc == null) {
			interaction.reply("Voice Channel not found! Join Audio Channel!").complete().deleteOriginal().queueAfter(3,TimeUnit.SECONDS);
			return;
		}
		String hash = "#"+(int)(Math.random()*10000);
		while(SongQuiz.getGame(hash)!=null) {
			hash = "#"+(int)(Math.random()*10000);
		}
		new SongQuiz(hash, channel, vc, interaction);

		//  else if (args.length == 5) {
		//	String hash = "#"+(int)(Math.random()*10000);
		//	while(SongQuiz.getGame(hash)!=null) {
		//		hash = "#"+(int)(Math.random()*10000);
		//	}
		//	new SongQuiz(hash, channel, vc, Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		//}


	}

}
