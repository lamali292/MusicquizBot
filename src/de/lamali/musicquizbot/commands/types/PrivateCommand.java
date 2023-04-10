package de.lamali.musicquizbot.commands.types;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;

public interface PrivateCommand {
	public void performCommand(User u, PrivateChannel channel, Message message);
}
