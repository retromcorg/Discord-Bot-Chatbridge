package com.johnymuffin.discordbot;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import com.johnymuffin.discordcore.DiscordCore;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class DiscordBot extends JavaPlugin implements Listener, EventExecutor, EventListener {
	Logger logger;
	DiscordCore discord;
	String gameBridge = "";
	String serverName = "";
	Boolean botEnabled = false;
	private ConfigReader configReader;

	public static final String PLUGIN_FOLDER = "./plugins/DiscordBot";
	private File pluginFolder = new File("./plugins/DiscordBot");
	File config;

	public void onLoad() {
		this.config = new File(this.pluginFolder, "config.properties");
		this.logger = getServer().getLogger();
		if (!this.pluginFolder.exists() || !this.pluginFolder.exists()) {
			this.pluginFolder.mkdirs();
		}

		if (!this.config.exists()) {
			try {
				this.config.createNewFile();
			} catch (IOException var2) {
				var2.printStackTrace();
			}
		}
	}

	public void onEnable() {
		this.configReader = new ConfigReader(this);
		this.logger = this.getServer().getLogger();
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_CHAT, this, this, Priority.Low, this);
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, this, this, Priority.Low, this);
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, this, this, Priority.Low, this);
		this.getServer().getPluginManager().registerEvent(Type.PLAYER_KICK, this, this, Priority.Low, this);
		this.logger.info("[DiscordBot] Enabling DiscordBot...");
		discord = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
		gameBridge = this.configReader.getChannel();
		serverName = this.configReader.getServerName();
		if (!gameBridge.isEmpty() && !serverName.isEmpty()) {
			botEnabled = true;
		} else {
			this.logger.info("---------------------------[DiscordBot]---------------------------");
			this.logger.info("Please provide a Servername and Channel for the Link");
			this.logger.info("------------------------------------------------------------------");
		}

		discord.Discord().DiscordSendToChannel(gameBridge, "**SERVER HAS STARTED** :yes:");
		discord.Discord().jda.addEventListener(this);

	}

	public void onDisable() {
		discord.Discord().DiscordSendToChannel(gameBridge, "**SERVER HAS STOPPED** :no:");
		this.logger.info("[DiscordBot] Successfully stopped!");
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (botEnabled) {
			if (event instanceof PlayerChatEvent) {
				discord.Discord().DiscordSendToChannel(gameBridge,
						"**" + ((PlayerChatEvent) event).getPlayer().getName() + "**: "
								+ ((PlayerChatEvent) event).getMessage());

			} else if (event instanceof PlayerJoinEvent) {
				discord.Discord().DiscordSendToChannel(gameBridge,
						"**" + ((PlayerJoinEvent) event).getPlayer().getName() + "** Has Joined The Game ["
								+ Bukkit.getServer().getOnlinePlayers().length + "/"
								+ Bukkit.getServer().getMaxPlayers() + "]");

			} else if (event instanceof PlayerQuitEvent) {
				int playercount = Bukkit.getServer().getOnlinePlayers().length - 1;
				discord.Discord().DiscordSendToChannel(gameBridge,
						"**" + ((PlayerQuitEvent) event).getPlayer().getName() + "** Has Left The Game [" + playercount
								+ "/" + Bukkit.getServer().getMaxPlayers() + "]");

			}

		}
	}

	@Override
	public void onEvent(net.dv8tion.jda.core.events.Event event) {
		// TODO Auto-generated method stub
		if(event instanceof GuildMessageReceivedEvent) {
			System.out.println(((GuildMessageReceivedEvent) event).getMessage().getContentRaw());
			if (((GuildMessageReceivedEvent) event).getAuthor().isBot() || ((GuildMessageReceivedEvent) event).getAuthor().isFake())
				return;
			TextChannel textChannel = discord.Discord().jda.getTextChannelById(gameBridge);
			String[] messageCMD = ((GuildMessageReceivedEvent) event).getMessage().getContentRaw().split(" ");
			if (((GenericGuildMessageEvent) event).getChannel() == textChannel) {
				String message = ((GuildMessageReceivedEvent) event).getMessage().getContentRaw();
				User author = ((GuildMessageReceivedEvent) event).getAuthor();
				message = "&f[&6Discord&f]&7" + author.getName() + ": " + message;
				message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
				Bukkit.getServer().broadcastMessage(message);
			}
		}
		
	}
}