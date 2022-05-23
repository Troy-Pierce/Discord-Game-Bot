package cfd.hireme.discord.utilites;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.dv8tion.jda.api.entities.Guild;

public class ConfigurationHandler {
	private final Instant DATE = Instant.now();
	private final File PATH_DIR = new File("Data");
	private final File LOG_DIR = new File(this.PATH_DIR.getAbsolutePath() + "/Logs");
	private final File LOG_FILE = new File(
			this.LOG_DIR.getAbsolutePath() + "/Log_" + this.DATE.toString().substring(0, DATE.toString().indexOf('T')));
	private final File GUILD_FILE = new File(this.PATH_DIR.getAbsolutePath() + "/Guilds.json");
	private final File INFORMATION_FILE = new File(this.PATH_DIR.getAbsolutePath() + "/Information.json");

	public ConfigurationHandler() throws IOException {
		// File Creation
		if (!this.PATH_DIR.exists()) {
			this.PATH_DIR.mkdirs();
		}
		if (!this.LOG_DIR.exists()) {
			this.LOG_DIR.mkdirs();
		}
		if (!this.LOG_FILE.exists()) {
			this.LOG_FILE.createNewFile();
		}
		if (!this.GUILD_FILE.exists()) {
			this.GUILD_FILE.createNewFile();
		}
		if (!this.INFORMATION_FILE.exists()) {
			this.INFORMATION_FILE.createNewFile();
		}
		// Error Logging
		PrintStream printStream = new PrintStream(new FileOutputStream(this.LOG_FILE.getAbsolutePath(), true), true);
		System.setErr(printStream);
		System.setOut(printStream);

	}

	public JsonObject getDefaultGuildSettings() {
		JsonObject object = new JsonObject();
		object.addProperty("gameChannel", "0");
		object.addProperty("alertRole", "0");
		return object;
	}

	public JsonObject getInformation() {
		Gson gson = new Gson();
		JsonObject storageObj = null;
		try (FileReader reader = new FileReader(this.INFORMATION_FILE)) {
			int c = 0;
			String line = "";
			while ((c = reader.read()) != -1) {
				line = line.concat(new String(Character.toChars(c + 20)));
			}
			System.out.println("String: " + line);
			storageObj = gson.fromJson(line, JsonObject.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (storageObj == null) {
			System.out.println("error reader");
			storageObj = new JsonObject();
			storageObj.add("epicReader", null);
			storageObj.add("ubiReader", null);
			storageObj.add("steamReader", null);
			try (FileWriter writer = new FileWriter(this.INFORMATION_FILE)) {
				String jsonString = gson.toJson(storageObj);
				String encryptString = "";
				for (int i = 0; i < jsonString.length(); i++) {
					encryptString = encryptString.concat(new String(Character.toChars(jsonString.charAt(i) - 20)));
				}
				writer.write(encryptString);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return storageObj;
	}

	public void setInformation(JsonObject object) {
		try (FileWriter writer = new FileWriter(this.INFORMATION_FILE)) {
			String jsonString = new Gson().toJson(object);
			String encryptString = "";
			for (int i = 0; i < jsonString.length(); i++) {
				encryptString = encryptString.concat(new String(Character.toChars(jsonString.charAt(i) - 20)));
			}
			writer.write(encryptString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JsonObject getStorage() {
		Gson gson = new Gson();
		JsonObject storageObj = null;
		try (FileReader reader = new FileReader(this.GUILD_FILE)) {
			storageObj = gson.fromJson(reader, JsonObject.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (storageObj == null) {
			storageObj = new JsonObject();
			try (FileWriter writer = new FileWriter(this.GUILD_FILE)) {
				writer.write(gson.toJson(storageObj));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return storageObj;
	}

	public void setStorage(JsonObject object) {
		try (FileWriter writer = new FileWriter(this.GUILD_FILE)) {
			writer.write(new Gson().toJson(object));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean hasGuildSettings(Guild guild) {
		return this.getStorage().has(guild.getId());
	}

	public JsonObject getGuildSettings(Guild guild) throws IOException {
		JsonObject storageObj = this.getStorage();
		JsonObject guildObj;
		Gson gson = new Gson();
		if (!storageObj.has(guild.getId())) {

			guildObj = this.getDefaultGuildSettings();
			storageObj.addProperty(guild.getId(), gson.toJson(guildObj));
			this.setStorage(storageObj);
		} else {
			guildObj = storageObj.get(guild.getId()).getAsJsonObject();
		}
		return guildObj;
	}

	public void setGuildSettings(Guild guild, JsonObject guildObj) throws IOException {
		JsonObject storageObj = this.getStorage();
		storageObj.add(guild.getId(), guildObj);
		this.setStorage(storageObj);
	}
}
