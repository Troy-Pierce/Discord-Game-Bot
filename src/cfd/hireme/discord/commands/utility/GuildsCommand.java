package cfd.hireme.discord.commands.utility;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import cfd.hireme.discord.commands.ICommand;
import cfd.hireme.discord.commands.data.MessageData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GuildsCommand implements ICommand {

	@Override
	public void handle(MessageData data, MessageReceivedEvent e) {
		// TODO Auto-generated method stub
		String string = "";
		for (Guild arg : GameAlert.getJDA().getGuilds()) {
			string = string + arg.getName() + " | " + arg.getOwner().getUser().getAsTag() + "\n";
		}
		e.getMessage().reply(string).queue();
	}

	@Override
	public String getCall() {
		// TODO Auto-generated method stub
		return "guilds";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "displays guilds the bot is in";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "guilds";
	}

	@Override
	public boolean isGuildRestricted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeveloperLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Permission[] getPermissions() {
		// TODO Auto-generated method stub
		return new Permission[] {};
	}

	@Override
	public CategoryType getCategory() {
		// TODO Auto-generated method stub
		return CategoryType.UTILTIES;
	}

}
