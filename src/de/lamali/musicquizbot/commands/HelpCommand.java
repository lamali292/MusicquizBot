package de.lamali.musicquizbot.commands;

import java.awt.Color;
import java.time.Instant;

import de.lamali.musicquizbot.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class HelpCommand implements ServerCommand{
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Color.gray);
		embed.setTimestamp(Instant.now());
		embed.setTitle("Commands:");
		embed.setDescription("[]: required \n (): unrequired");

		embed.addField("Stuff", "!help \n !quiz start (lobbytime) (requesttime) (guesstime)\n !quiz join [id]\n !quiz end [id]", true);
		embed.addField("", "open help menu \n start song quiz lobby\n join song quiz lobby\n end lobby", true);

		channel.sendMessageEmbeds(embed.build()).queue();
		message.delete().queue();

	}

}
