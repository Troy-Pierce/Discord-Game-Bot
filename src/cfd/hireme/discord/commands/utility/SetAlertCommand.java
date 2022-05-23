package cfd.hireme.discord.commands.utility;

import java.io.IOException;

import com.google.gson.JsonObject;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import cfd.hireme.discord.commands.ICommand;
import cfd.hireme.discord.commands.data.MessageData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetAlertCommand implements ICommand {

	@Override
	public void handle(MessageData data, MessageReceivedEvent e) {
		// TODO Auto-generated method stub
		EmbedBuilder builder = null;
		try {
			if (e.getMessage().getMentionedRoles().size() > 0) {
				JsonObject guildObj = GameAlert.getConfigurationHandler().getGuildSettings(e.getGuild());
				guildObj.addProperty("alertRole", e.getMessage().getMentionedRoles().get(0).getId());
				GameAlert.getConfigurationHandler().setGuildSettings(e.getGuild(), guildObj);
				builder = GameAlert.getCommandHandler().getSuccessEmbed("Set Role!", e.getAuthor(), e.getGuild(),
						new Field("Role", e.getMessage().getMentionedRoles().get(0).getAsMention(), false));
			} else {
				if (data.getArgs()[0].toLowerCase().equals("none")) {
					JsonObject guildObj = GameAlert.getConfigurationHandler().getGuildSettings(e.getGuild());
					guildObj.addProperty("alertRole", "0");
					GameAlert.getConfigurationHandler().setGuildSettings(e.getGuild(), guildObj);
					builder = GameAlert.getCommandHandler().getSuccessEmbed("Removed Role!", e.getAuthor(),
							e.getGuild(), new Field("Role", "None", false));
				} else {
					builder = GameAlert.getCommandHandler().getErrorEmbed("Must mention a role!", e.getAuthor(),
							e.getGuild());
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			builder = GameAlert.getCommandHandler().getErrorEmbed(e1.toString(), e.getAuthor(), e.getGuild());
			e1.printStackTrace();
		}
		e.getMessage().replyEmbeds(builder.build()).queue();
	}

	@Override
	public String getCall() {
		// TODO Auto-generated method stub
		return "SetAlert";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Sets the alert role to be mentioned when a new game is posted\n<@Role> = Mention | None = Removes role";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "SetAlert <@Role>/None";
	}

	@Override
	public boolean isGuildRestricted() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isDeveloperLocked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Permission[] getPermissions() {
		// TODO Auto-generated method stub
		return new Permission[] { Permission.MANAGE_SERVER };
	}

	@Override
	public CategoryType getCategory() {
		// TODO Auto-generated method stub
		return CategoryType.ALERTS;
	}

}
