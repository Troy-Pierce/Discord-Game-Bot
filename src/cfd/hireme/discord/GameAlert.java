package cfd.hireme.discord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import cfd.hireme.discord.commands.CommandHandler;
import cfd.hireme.discord.listeners.OnLeave;
import cfd.hireme.discord.listeners.OnMessage;
import cfd.hireme.discord.utilites.ConfigurationHandler;
import cfd.hireme.discord.utilites.GameHandler;
import cfd.hireme.discord.utilites.Keys;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class GameAlert {
	private static JDA jda;
	private static CommandHandler commandHandler;
	private static ConfigurationHandler configurationHandler;
	private static GameHandler gameHandler;
	private static Timer timer = new Timer();

	public static void main(String[] args) {
		try {
			Collection<GatewayIntent> intents = new ArrayList<GatewayIntent>();
			intents.add(GatewayIntent.DIRECT_MESSAGES);
			intents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
			intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
			intents.add(GatewayIntent.GUILD_MESSAGES);
			intents.add(GatewayIntent.GUILD_MEMBERS);
			jda = JDABuilder.create(Keys.BOT_TOKEN_BETA, intents).build();
			intents = null;
			jda.awaitReady();
			configurationHandler = new ConfigurationHandler();

			System.out.println("--------------------------------");
			System.out.println("Starting");
			System.out.println("JDA Version - " + JDAInfo.VERSION);
			System.out.println("--------------------------------");

			commandHandler = new CommandHandler();
			gameHandler = new GameHandler();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					gameHandler.checkGames();
				}

			}, 1000, 86400000);

			jda.addEventListener(new OnMessage());
			jda.addEventListener(new OnLeave());

			jda.getPresence().setActivity(Activity.listening("@" + jda.getSelfUser().getAsTag() + " help"));
			System.out.println("Bot ready!\nPrefix: " + Keys.PREFIXES[0]);
		} catch (LoginException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// ConfigurationHandler
			e.printStackTrace();
		}

	}

	public static JDA getJDA() {
		return jda;
	}

	public static CommandHandler getCommandHandler() {
		return commandHandler;
	}

	public static ConfigurationHandler getConfigurationHandler() {
		return configurationHandler;
	}

	public static GameHandler getGameHandler() {
		return gameHandler;
	}
}
