package cfd.hireme.discord.utilites;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cfd.hireme.discord.GameAlert;
import cfd.hireme.discord.commands.data.StoreItem;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class GameHandler {
	private HashMap<String, HttpReader> storeMap = new HashMap<String, HttpReader>();
	int cycle = 0;

	public GameHandler() throws IOException, InterruptedException {
//		(HttpReader) new Gson().fromJson(
//				GameAlert.getConfigurationHandler().getInformation().get("epicStoreObject"),
//				HttpReader.class);
		// .get("epicStoreObject")
		JsonObject information = GameAlert.getConfigurationHandler().getInformation();
		if (information.has("epicReader") && !information.get("epicReader").isJsonNull()) {
			Gson gson = new Gson();
			this.storeMap.put("epicReader",
					gson.fromJson(information.get("epicReader").getAsString(), HttpReader.class));
		} else {
			this.storeMap.put("epicReader",
					new HttpReader(Keys.EPIC_GAME_BACKEND_API, HttpReader.StoreType.EPIC, true, true));
		}
		if (information.has("ubiReader") && !information.get("ubiReader").isJsonNull()) {
			Gson gson = new Gson();
			this.storeMap.put("ubiReader", gson.fromJson(information.get("ubiReader").getAsString(), HttpReader.class));
		} else {
			this.storeMap.put("ubiReader",
					new HttpReader(Keys.UBI_GAME_SITE, HttpReader.StoreType.UBISOFT, false, true));
		}
		if (information.has("steamReader") && !information.get("steamReader").isJsonNull()) {
			Gson gson = new Gson();
			this.storeMap.put("steamReader",
					gson.fromJson(information.get("steamReader").getAsString(), HttpReader.class));
		} else {
			this.storeMap.put("steamReader",
					new HttpReader(Keys.STEAM_STORE_API, HttpReader.StoreType.STEAM, true, true));
		}
		// sleep(86400000);
	}

	public void checkGames() {
		try {
			cycle++;
			System.out.println("Cycle " + cycle + " - " + Instant.now().toString());
			JsonObject obj = GameAlert.getConfigurationHandler().getInformation();
			List<StoreItem> queuedItems = new ArrayList<StoreItem>();
			for (Entry<String, HttpReader> entry : this.storeMap.entrySet()) {
				HttpReader readerNew = new HttpReader(entry.getValue().httpUrl, entry.getValue().storeType,
						entry.getValue().api, false);
				if (entry.getValue() != null && !entry.getValue().isNewReader) {
					// DEBUG - GAME
					// epicReaderNew.getStoreItems().add(new StoreItem("Minecraft 2 - Test",
					// "Mojang", 9999L, 1000L, "USD",
					// 2L, "https://www.google.com/",
					// "https://upload.wikimedia.org/wikipedia/en/thumb/5/51/Minecraft_cover.png/220px-Minecraft_cover.png",
					// "2018-11-27T21:25:00.000Z"));

					for (StoreItem item : readerNew.getStoreItems()) {
						boolean hasItem = false;
						for (StoreItem itemSecond : entry.getValue().getStoreItems()) {
							if (item.getTitle().equals(itemSecond.getTitle())) {
								hasItem = true;
							}
						}
						if (!hasItem) {
							queuedItems.add(item);
						}
					}
				} else {
					for (StoreItem item : readerNew.getStoreItems()) {
						queuedItems.add(item);
					}
				}
				entry.setValue(readerNew);
				obj.addProperty(entry.getKey(), new Gson().toJson(entry.getValue()));
			}
			if (queuedItems.size() > 0) {
				this.queueListedItems(queuedItems);
			}
			GameAlert.getConfigurationHandler().setInformation(obj);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void queueListedItems(List<StoreItem> itemList) {
		Thread queueThread = new Thread() {
			@Override
			public void run() {
				if (itemList.size() > 0) {
					for (StoreItem item : itemList) {
						try {
							postGameGlobal(item);
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					itemList.clear();
				}
			}
		};
		queueThread.setDaemon(true);
		queueThread.setPriority(Thread.MIN_PRIORITY);
		queueThread.start();
	}

	public StoreItem[] getEpicStoreItems() {
		return this.storeMap.get("epicReader").getStoreItems()
				.toArray(new StoreItem[this.storeMap.get("epicReader").getStoreItems().size()]);
	}

	public StoreItem[] getUbiStoreItems() {
		return this.storeMap.get("ubiReader").getStoreItems()
				.toArray(new StoreItem[this.storeMap.get("ubiReader").getStoreItems().size()]);
	}

	public StoreItem[] getSteamItems() {
		return this.storeMap.get("steamReader").getStoreItems()
				.toArray(new StoreItem[this.storeMap.get("steamReader").getStoreItems().size()]);
	}

	public void postGameGlobal(StoreItem item) {
		JsonObject object = GameAlert.getConfigurationHandler().getStorage();
		for (Guild guild : GameAlert.getJDA().getGuilds()) {
			if (GameAlert.getConfigurationHandler().hasGuildSettings(guild)) {
				TextChannel gameChannel = GameAlert.getJDA().getTextChannelById(
						object.get(guild.getId()).getAsJsonObject().get("gameChannel").getAsString());
				if (gameChannel != null) {
					Role alertRole = GameAlert.getJDA()
							.getRoleById(object.get(guild.getId()).getAsJsonObject().get("alertRole").getAsString());
					if (alertRole != null) {
						gameChannel.sendMessage(alertRole.getAsMention()).queue();
					}
					gameChannel.sendMessageEmbeds(item.getEmbed().build()).queue();
				}
			}
		}
	}
}
