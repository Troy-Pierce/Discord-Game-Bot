package cfd.hireme.discord.listeners;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnMessage extends ListenerAdapter {
	@Override
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().isBot()) {
			return;
		}
		for (String prefix : Keys.PREFIXES) {
			if (e.getMessage().getContentRaw().startsWith(prefix)) {
				GameAlert.getCommandHandler().handleCommand(e);
				return;
			}
		}
	}
}
