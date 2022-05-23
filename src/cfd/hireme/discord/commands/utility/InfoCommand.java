package cfd.hireme.discord.commands.utility;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import cfd.hireme.discord.commands.ICommand;
import cfd.hireme.discord.commands.data.MessageData;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InfoCommand implements ICommand {
	@Override
	public void handle(MessageData data, MessageReceivedEvent e) {
		// TODO Auto-generated method stub
		Field field = new Field("Discord Information",
				String.format("Guilds: `%s`\nUsers: `%s`\nBot Invite: %s\nGuild Invite: %s",
						GameAlert.getJDA().getGuildCache().size(), GameAlert.getJDA().getUserCache().size(),
						Keys.EMBED_BOT_INVITE, Keys.EMBED_GUILD_INVITE),
				true);
		Field field2 = new Field("Host Information", String.format(
				"Memory: `%s/%s MiB`\nSystem Memory: `%s/%s GiB`\nInvite: %s",
				Double.toString(Math
						.round(((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1.049e6))),
				Double.toString(Math.round((Runtime.getRuntime().totalMemory() / 1.049e6))),
				Double.toString(Math.round((((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
						.getTotalPhysicalMemorySize()
						- ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
								.getFreePhysicalMemorySize())
						/ 1.074e9)),
				Double.toString(Math.round(((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
						.getTotalPhysicalMemorySize() / 1.074e+9)),
				Keys.EMBED_HOST_GUILD_INVITE), true);

		TextChannel channel;
		try {
			if (GameAlert.getConfigurationHandler().hasGuildSettings(e.getGuild())) {
				channel = GameAlert.getJDA().getTextChannelById(GameAlert.getConfigurationHandler()
						.getGuildSettings(e.getGuild()).get("gameChannel").getAsString());
			} else {
				channel = null;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			channel = null;

		}
		Role role;
		try {
			if (GameAlert.getConfigurationHandler().hasGuildSettings(e.getGuild())) {
				role = GameAlert.getJDA().getRoleById(GameAlert.getConfigurationHandler().getGuildSettings(e.getGuild())
						.get("alertRole").getAsString());
			} else {
				role = null;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			role = null;
		}
		Field field3 = new Field("Guild Information", String.format("Alert Channel: %s\nAlert Role: %s",
				channel != null ? channel.getAsMention() : "`None`", role != null ? role.getAsMention() : "`None`"),
				true);

		e.getMessage()
				.replyEmbeds(GameAlert.getCommandHandler()
						.getSuccessEmbed("Bot Information", e.getAuthor(), e.getGuild(), field, field2, field3).build())
				.queue();
	}

	@Override
	public String getCall() {
		// TODO Auto-generated method stub
		return "info";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "information about the bot";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "info";
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
		return new Permission[] {};
	}

	@Override
	public CategoryType getCategory() {
		// TODO Auto-generated method stub
		return CategoryType.UTILTIES;
	}

}
