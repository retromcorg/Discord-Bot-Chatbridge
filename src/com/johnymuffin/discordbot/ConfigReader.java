package com.johnymuffin.discordbot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class ConfigReader {
	// New Config
	private static String token = "";
	private static String serverName = "";
	private static String channelID = "";

	ConfigReader(DiscordBot instance) {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(instance.config));
		} catch (FileNotFoundException var4) {
			instance.logger.warning("[RetroBot] No properties found! Making new file...");
			var4.printStackTrace();
		} catch (IOException var5) {
			var5.printStackTrace();
		}
		// New Config
		serverName = prop.getProperty("serverName", serverName);
		channelID = prop.getProperty("channelID", channelID);
		prop.setProperty("channelID", channelID);
		prop.setProperty("serverName", serverName);

		try {
			prop.store(new FileOutputStream(instance.config), "Properties for DiscordBot");
		} catch (Exception var3) {
			instance.logger.severe("Failed to save properties for RetroBot!");
			var3.printStackTrace();
		}
	}

	String getServerName() {
		return serverName;
	}
	
	String getChannel() {
		return channelID;
	}

	void onDisable() {
		channelID = null;
		serverName = null;
	}
}
