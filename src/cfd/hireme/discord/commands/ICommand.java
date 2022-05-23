package cfd.hireme.discord.commands;

import cfd.hireme.discord.commands.data.MessageData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ICommand {
	void handle(MessageData data, MessageReceivedEvent e);

	String getCall();

	String getDescription();

	String getUsage();

	boolean isGuildRestricted();

	boolean isDeveloperLocked();

	Permission[] getPermissions();

	CategoryType getCategory();
}
