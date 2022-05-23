package cfd.hireme.discord.commands.utility.help_command;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class HelpListener extends ListenerAdapter {
	private Message message;
	private String ownerId;
	HelpCommand.timeoutThread threadTimeout;

	public HelpListener(Message message, String ownerId) {
		this.message = message;
		this.ownerId = ownerId;
		this.threadTimeout = new HelpCommand.timeoutThread(this);
		this.threadTimeout.start();
		GameAlert.getJDA().addEventListener(this);

	}

	public void stopReactions() {
		if (this.message.getChannelType() == ChannelType.TEXT || this.message.getChannelType() == ChannelType.STORE) {
			try {
				this.message.clearReactions().queue();
			} catch (Exception e) {

			}
		}
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(this.message.getEmbeds().get(0).getColor());
		embed.setTitle(this.message.getEmbeds().get(0).getTitle());
		for (Field field : this.message.getEmbeds().get(0).getFields()) {
			embed.addField(field);
		}
		embed.setFooter("Timed out!");
		this.message.editMessageEmbeds(embed.build()).queue();
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getMessageId().equals(this.message.getId())) {
			if (event.getUser().getId().equals(this.ownerId)) {
				if (CategoryType.hasEmoji(event.getReactionEmote().getEmoji())) {
					this.threadTimeout.resetTimer();
					this.message.editMessageEmbeds(CategoryType.getFromEmoji(event.getReactionEmote().getEmoji())
							.getHelpPage(event.getUser()).build()).queue();
					try {
						this.message.removeReaction(event.getReactionEmote().getEmoji(), event.getUser()).queue();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
