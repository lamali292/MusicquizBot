package de.lamali.musicquizbot;

import java.util.concurrent.ConcurrentHashMap;

import de.lamali.musicquizbot.commands.*;
import de.lamali.musicquizbot.commands.types.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class CommandManager {
	public ConcurrentHashMap<String, ServerCommand> serverCommands;
	public ConcurrentHashMap<String, PrivateCommand> privateCommands;

	public CommandManager() {
		this.serverCommands = new ConcurrentHashMap<>();
		this.privateCommands = new ConcurrentHashMap<>();

		this.serverCommands.put("help", new HelpCommand());
		this.serverCommands.put("quiz", new QuizCommand());
	}

	public boolean performServerCommand(String command, Member m, TextChannel channel, Message message) {
		ServerCommand cmd;

		if ((cmd = this.serverCommands.get(command.toLowerCase())) != null) {
			cmd.performCommand(m, channel, message);
			return true;
		}

		return false;
	}

	public boolean performPrivateCommand(String command, User m, PrivateChannel channel, Message message) {
		PrivateCommand cmd;

		if ((cmd = this.privateCommands.get(command.toLowerCase())) != null) {
			cmd.performCommand(m, channel, message);
			return true;
		}

		return false;
	}
}
