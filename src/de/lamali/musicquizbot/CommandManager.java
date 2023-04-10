package de.lamali.musicquizbot;

import java.util.concurrent.ConcurrentHashMap;

import de.lamali.musicquizbot.commands.*;
import de.lamali.musicquizbot.commands.types.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class CommandManager {
	public ConcurrentHashMap<String, GuildCommand> serverCommands;
	public ConcurrentHashMap<String, GlobalCommand> privateCommands;

	public CommandManager() {
		this.serverCommands = new ConcurrentHashMap<>();
		this.privateCommands = new ConcurrentHashMap<>();

		this.serverCommands.put("help", new HelpCommand());
		this.serverCommands.put("quiz", new QuizCommand());
	}

	public boolean performGuildCommand(String command, SlashCommandInteraction event) {
		GuildCommand cmd;

		if ((cmd = this.serverCommands.get(command)) != null) {
			cmd.performCommand(event);
			return true;
		}

		return false;
	}

	public boolean performPrivateCommand(String command, User m, PrivateChannel channel, Message message) {
		GlobalCommand cmd;

		if ((cmd = this.privateCommands.get(command.toLowerCase())) != null) {
			cmd.performCommand(m, channel, message);
			return true;
		}

		return false;
	}
}
