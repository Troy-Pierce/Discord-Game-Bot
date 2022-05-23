package cfd.hireme.discord.commands;

import java.util.HashMap;
import java.util.Map;

import cfd.hireme.discord.commands.data.MessageData;
import cfd.hireme.discord.commands.utility.GuildsCommand;
import cfd.hireme.discord.commands.utility.InfoCommand;
import cfd.hireme.discord.commands.utility.SetAlertCommand;
import cfd.hireme.discord.commands.utility.SetChannelCommand;
import cfd.hireme.discord.commands.utility.help_command.HelpCommand;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHandler {
	private final Map<String, ICommand> COMMANDS = new HashMap<String, ICommand>();

	public CommandHandler() {
		this.createCommand(new HelpCommand());
		this.createCommand(new InfoCommand());
		this.createCommand(new SetAlertCommand());
		this.createCommand(new SetChannelCommand());
		this.createCommand(new GuildsCommand());
	}

	private void createCommand(ICommand command) {
		if (!this.COMMANDS.containsKey(command.getCall())) {
			this.COMMANDS.put(command.getCall().toLowerCase(), command);
			System.out.println("Registered Command: " + command.getCall().toLowerCase());
		} else {
			System.out.println("Error: Command Call already registered!\nCall: " + command.getCall().toLowerCase());
		}
	}

	public Map<String, ICommand> getCommands() {
		return this.COMMANDS;
	}

	public void handleCommand(MessageReceivedEvent e) {
		for (String prefix : Keys.PREFIXES) {
			if (e.getMessage().getContentRaw().startsWith(prefix)) {
				String[] args = e.getMessage().getContentRaw().split(" ");
				/*
				 * args[0] - prefix args[1] - call args[2] - extra
				 */
				if (args.length > 1) {
					if (args[1] != null) {
						if (this.COMMANDS.containsKey(args[1].toLowerCase())) {
							ICommand command = this.COMMANDS.get(args[1].toLowerCase());
							if (command.isGuildRestricted()) {
								if (!e.isFromGuild()) {
									e.getChannel().sendMessageEmbeds(
											this.getErrorEmbed("Guild Only Command!", e.getAuthor(), null).build())
											.queue();
									;
									return;
								}
							}
							if (command.isDeveloperLocked()) {
								for (String key : Keys.DEVELOPER_KEY) {
									if (key.equals(e.getAuthor().getId())) {
										command.handle(new MessageData(e), e);
										return;
									}
								}
								e.getChannel().sendMessageEmbeds(this.getErrorEmbed("Developer Locked Command", e.getAuthor(),
										e.isFromGuild() ? e.getGuild() : null).build()).queue();
								;
								return;
							} else {
								if (command.getPermissions().length > 0 && command.isGuildRestricted()) {
									for (Permission permission : command.getPermissions()) {
										if (!e.getMember().hasPermission(permission)) {
											e.getChannel().sendMessageEmbeds(this.getErrorEmbed(
													String.format("Invalid Permissions\n`%s`", permission.name()),
													e.getAuthor(), e.getGuild()).build()).queue();
											return;
										}
									}
								}
								System.out.println("Handling: \"" + command.getCall() + "\" by \""
										+ e.getAuthor().getAsTag() + "\"");
								command.handle(new MessageData(e), e);
							}
						}
					}
				}
			}
		}
	}

	public EmbedBuilder getSuccessEmbed(String title, User user, Guild guild, Field... fields) {
		EmbedBuilder embed = new EmbedBuilder();
		String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getDefaultAvatarUrl();
		embed.setFooter(user.getAsTag(), avatar);
		if (guild != null) {
			embed.setThumbnail(guild.getIconUrl());
		} else {
			embed.setThumbnail(avatar);
		}
		embed.setAuthor(title);
		embed.setColor(Keys.MESSAGE_SUCCESS);
		for (Field field : fields) {
			embed.addField(field);
		}
		return embed;
	}

	public EmbedBuilder getErrorEmbed(String text, User user, Guild guild) {
		EmbedBuilder embed = new EmbedBuilder();
		String avatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getDefaultAvatarUrl();
		embed.setFooter(user.getAsTag(), avatar);
		if (guild != null) {
			embed.setThumbnail(guild.getIconUrl());
		} else {
			embed.setThumbnail(avatar);
		}
		embed.setAuthor("Error");
		embed.setColor(Keys.MESSAGE_ERROR);
		embed.addField("Details", text, true);
		return embed;
	}
}
