package cfd.hireme.discord.commands.utility;

import java.io.IOException;

import com.google.gson.JsonObject;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import cfd.hireme.discord.commands.ICommand;
import cfd.hireme.discord.commands.data.MessageData;
import cfd.hireme.discord.commands.data.StoreItem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetChannelCommand implements ICommand {

	@Override
	public void handle(MessageData data, MessageReceivedEvent e) {
		// TODO Auto-generated method stub
		EmbedBuilder builder = null;
		try {
			if (e.getMessage().getMentionedChannels().size() > 0) {
				JsonObject guildObj = GameAlert.getConfigurationHandler().getGuildSettings(e.getGuild());
				if (!guildObj.get("gameChannel").getAsString()
						.equals(e.getMessage().getMentionedChannels().get(0).getId())) {

					Role alertRole = e.getGuild().getRoleById(guildObj.get("alertRole").getAsString());
					if (alertRole != null) {
						e.getMessage().getMentionedChannels().get(0).sendMessage(alertRole.getAsMention()).queue();
					}
					Thread thread = new Thread() {
						@Override
						public void run() {
							for (StoreItem item : GameAlert.getGameHandler().getEpicStoreItems()) {
								try {
									e.getMessage().getMentionedChannels().get(0).sendMessageEmbeds(item.getEmbed().build())
											.queue();
									Thread.sleep(10000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							for (StoreItem item : GameAlert.getGameHandler().getUbiStoreItems()) {
								try {
									e.getMessage().getMentionedChannels().get(0).sendMessageEmbeds(item.getEmbed().build())
											.queue();
									Thread.sleep(10000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							for (StoreItem item : GameAlert.getGameHandler().getSteamItems()) {
								try {
									e.getMessage().getMentionedChannels().get(0).sendMessageEmbeds(item.getEmbed().build())
											.queue();
									Thread.sleep(10000);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					};
					thread.setDaemon(true);
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.start();
					guildObj.addProperty("gameChannel", e.getMessage().getMentionedChannels().get(0).getId());
					GameAlert.getConfigurationHandler().setGuildSettings(e.getGuild(), guildObj);
					builder = GameAlert.getCommandHandler().getSuccessEmbed("Set Channel!", e.getAuthor(), e.getGuild(),
							new Field("Channel", e.getMessage().getMentionedChannels().get(0).getAsMention(), false));
				} else {
					builder = GameAlert.getCommandHandler().getErrorEmbed(
							"Game channel is already the mentioned channel!", e.getAuthor(), e.getGuild());
				}

			} else if (data.getArgs()[0].toLowerCase().equals("none")) {
				JsonObject guildObj = GameAlert.getConfigurationHandler().getGuildSettings(e.getGuild());
				guildObj.addProperty("gameChannel", "0");
				GameAlert.getConfigurationHandler().setGuildSettings(e.getGuild(), guildObj);
				builder = GameAlert.getCommandHandler().getSuccessEmbed("Set Channel!", e.getAuthor(), e.getGuild(),
						new Field("Channel", "None", false));
			} else {
				builder = GameAlert.getCommandHandler().getErrorEmbed("Must mention a channel!", e.getAuthor(),
						e.getGuild());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			builder = GameAlert.getCommandHandler().getErrorEmbed(
					e1.getCause().toString() + "\n" + e1.getCause().getMessage(), e.getAuthor(), e.getGuild());
			e1.printStackTrace();
		}
		e.getMessage().replyEmbeds(builder.build()).queue();
	}

	@Override
	public String getCall() {
		// TODO Auto-generated method stub
		return "SetChannel";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Sets the channel game alerts are posted in.\n <@Channel> = Sets channel | None = Removes channel";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "SetChannel <@Channel>/None";
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
