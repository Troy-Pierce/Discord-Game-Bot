package cfd.hireme.discord.listeners;

import com.google.gson.JsonObject;

import cfd.hireme.discord.GameAlert;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OnLeave extends ListenerAdapter {
	@Override
	public void onGuildLeave(GuildLeaveEvent e) {
		if (GameAlert.getConfigurationHandler().hasGuildSettings(e.getGuild())) {
			JsonObject object = GameAlert.getConfigurationHandler().getStorage();
			object.remove(e.getGuild().getId());
			GameAlert.getConfigurationHandler().setStorage(object);
		}
	}

}
