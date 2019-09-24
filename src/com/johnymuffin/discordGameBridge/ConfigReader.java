package com.johnymuffin.discordGameBridge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class ConfigReader {
	// New Config
	
	//discord.Discord().DiscordSendToChannel(gameBridge,
	//"**" + ((PlayerJoinEvent) event).getPlayer().getName() + "** Has Joined The Game ["
	//+ Bukkit.getServer().getOnlinePlayers().length + "/"
	//+ Bukkit.getServer().getMaxPlayers() + "]");

	private static String discordChatMessage = "&f[&6Discord&f]&7 %messageAuthor%: %message%";
	private static String gameChatMessage = "**%messageAuthor%**: %message%";
	private static String joinMessage = "**%username%** Has Joined THe Game [%onlineCount%/%maxCount%]";
	private static String quitMessage = "**%username%** Has Quit THe Game [%onlineCount%/%maxCount%]";
	
	private static String serverName = "";
	private static String channelID = "";
	private static Boolean PrensencePlayercount = Boolean.TRUE;
	private static Boolean OnlineCommand = Boolean.TRUE;

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
		PrensencePlayercount = Boolean.valueOf(Boolean.parseBoolean(prop.getProperty("PrensencePlayercount", "" + PrensencePlayercount)));
		OnlineCommand = Boolean.valueOf(Boolean.parseBoolean(prop.getProperty("OnlineCommand", "" + OnlineCommand)));
		//Join Custom Message
		joinMessage = prop.getProperty("joinMessage", joinMessage);
		prop.setProperty("joinMessage", joinMessage);
		//Quit Custom Message
		quitMessage = prop.getProperty("quitMessage", quitMessage);
		prop.setProperty("quitMessage", quitMessage);
		//Discord Chat Message
		discordChatMessage = prop.getProperty("discordChatMessage", discordChatMessage);
		prop.setProperty("discordChatMessage", discordChatMessage);
		//Game Chat Message
		gameChatMessage = prop.getProperty("gameChatMessage", gameChatMessage);
		prop.setProperty("gameChatMessage", gameChatMessage);
		
		prop.setProperty("OnlineCommand", "" + OnlineCommand);
		prop.setProperty("PrensencePlayercount", "" + PrensencePlayercount);
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
	
	String getJoinMessage() {
		return joinMessage;
	}
	String getQuitMessage() {
		return quitMessage;
	}
	String getDiscordChatMessage() {
		return discordChatMessage;
	}
	String getGameChatMessage() {
		return gameChatMessage;
	}
	
	Boolean getPrensencePlayercount() {
		return PrensencePlayercount;
		
	}
	Boolean getOnlineCommand() {
		return OnlineCommand;
		
	}

	void onDisable() {
		channelID = null;
		serverName = null;
	}
}
