package cfd.hireme.discord.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public enum CategoryType {
	CATEGORIES("ðŸŒ?", false), ALERTS("â?±ï¸?", false), UTILTIES("âš™ï¸?", false);
	private static Map<String, CategoryType> typeMap = new HashMap<String, CategoryType>();
	static {
		CategoryType[] types = values();
		for (CategoryType type : types) {
			if (!typeMap.containsKey(type.emoji) || !typeMap.containsValue(type)) {
				typeMap.put(type.emoji, type);
			} else {
				System.out.println("Category Type already Registered!");
			}
		}
	}
	private final String emoji;
	private final boolean isDisabled;

	CategoryType(String emoji, boolean disabled) {
		this.emoji = emoji;
		this.isDisabled = disabled;
	}

	public String getEmoji() {
		return this.emoji;
	}

	public boolean isDisabled() {
		return this.isDisabled;
	}

	public EmbedBuilder getHelpPage(User user) {
		if (typeMap.containsKey(this.emoji)) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setThumbnail(GameAlert.getJDA().getSelfUser().getAvatarUrl());
			embed.setFooter(user.getAsTag());
			embed.setTitle("Help Menu");
			embed.setColor(Keys.MESSAGE_SUCCESS);
			String description = "";
			if (!typeMap.get(this.emoji).name().toLowerCase().equals("categories")) {
				if (!this.isDisabled) {
					for (ICommand command : GameAlert.getCommandHandler().getCommands().values()) {
						if (command.getCategory() == this) {
							if (command.isDeveloperLocked()) {
								if (Arrays.asList(Keys.DEVELOPER_KEY).contains(user.getId())) {
									description += command.getUsage() + "\n";
								}
							} else {
								description += command.getUsage() + "\n";
							}
						}
					}
				} else {
					description += "Command Category is disabled";
				}
			} else {
				for (CategoryType type : CategoryType.values()) {
					description += String.format("%s %s\n", type.getEmoji(), CategoryType.getProperName(type));
				}
			}
			embed.addField(CategoryType.getProperName(this), description, false);
			return embed;
		} else {
			return null;
		}
	}

	public static boolean hasCategory(CategoryType type) {
		return typeMap.containsValue(type);
	}

	public static CategoryType getFromName(String name) {
		for (CategoryType type : typeMap.values()) {
			if (type.name().toUpperCase().equals(name.toUpperCase())) {
				return type;
			}
		}
		return null;
	}

	public static boolean hasEmoji(String emoji) {
		return typeMap.containsKey(emoji);
	}

	public static CategoryType getFromEmoji(String emoji) {
		return typeMap.get(emoji);
	}

	public static String getProperName(CategoryType type) {
		return type.name().substring(0, 1) + type.name().substring(1).toLowerCase();
	}
}
