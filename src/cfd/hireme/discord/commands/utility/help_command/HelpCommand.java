package cfd.hireme.discord.commands.utility.help_command;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.CategoryType;
import cfd.hireme.discord.commands.ICommand;
import cfd.hireme.discord.commands.data.MessageData;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand implements ICommand {

	public static class timeoutThread extends Thread {
		HelpListener adapter;
		int seconds = 0;

		public timeoutThread(HelpListener helpAdapter) {
			this.adapter = helpAdapter;
			this.setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
			try {
				while (seconds < 30) {
					Thread.sleep(1000);
					seconds++;
				}
				adapter.stopReactions();
				GameAlert.getJDA().removeEventListener(adapter);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void resetTimer() {
			this.seconds = 0;
		}
	}

	@Override
	public void handle(MessageData data, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		EmbedBuilder embed = null;
		boolean isCommand = false;
		if (data.getArgs().length == 0) {
			embed = CategoryType.CATEGORIES.getHelpPage(event.getAuthor());
		} else if (data.getArgs().length == 1) {
			if (CategoryType.getFromName(data.getArgs()[0]) != null) {
				embed = CategoryType.getFromName(data.getArgs()[0]).getHelpPage(event.getAuthor());
			} else {
				if (GameAlert.getCommandHandler().getCommands().containsKey(data.getArgs()[0].toLowerCase())) {
					String description = "";
					ICommand command = GameAlert.getCommandHandler().getCommands().get(data.getArgs()[0].toLowerCase());
					description += String.format("*%s*\n\nUsage: `%s`", command.getDescription(), command.getUsage());
					embed = new EmbedBuilder();
					embed.addField(String.format("**%s Command**", command.getCall()), description, false);
					embed.setTitle("Help Menu");
					embed.setColor(Keys.MESSAGE_SUCCESS);
					isCommand = true;

				}
			}
		}
//		Message message = new MessageBuilder(embed.build()).build();

		Message message = event.getMessage().replyEmbeds(embed.build()).submit().join();
		embed.clear();
		if (!isCommand) {
			int i = 0;
			for (CategoryType type : CategoryType.values()) {
				message.addReaction(type.getEmoji()).queue();
				i++;
				if (i == CategoryType.values().length) {
					new HelpListener(message, event.getAuthor().getId());
				}
			}

		}
	}

	@Override
	public String getCall() {
		// TODO Auto-generated method stub
		return "Help";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Shows GameAlerts commands!";
	}

	@Override
	public String getUsage() {
		// TODO Auto-generated method stub
		return "Help <Category/Command>";
	}

	@Override
	public CategoryType getCategory() {
		// TODO Auto-generated method stub
		return CategoryType.UTILTIES;
	}

	@Override
	public boolean isGuildRestricted() {
		// TODO Auto-generated method stub
		return false;
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

}
