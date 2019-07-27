package com.johnymuffin.discordbot;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import com.johnymuffin.discordcore.DiscordCore;
import com.nidefawl.Stats.Stats;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
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
	// Custom Messages
	String discordChatMessage = "";
	String gameChatMessage = "";
	String joinMessage = "";
	String quitMessage = "";

	Boolean botEnabled = false;
	private ConfigReader configReader;
	private int taskId = 0;

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
		// Custom Messages
		discordChatMessage = this.configReader.getDiscordChatMessage();
		gameChatMessage = this.configReader.getGameChatMessage();
		joinMessage = this.configReader.getJoinMessage();
		quitMessage = this.configReader.getQuitMessage();
		if (!gameBridge.isEmpty() && !serverName.isEmpty()) {
			botEnabled = true;
			discord.Discord().DiscordSendToChannel(gameBridge, "**SERVER HAS STARTED** :yes:");
			discord.Discord().jda.addEventListener(this);
		} else {
			this.logger.info("---------------------------[DiscordBot]---------------------------");
			this.logger.info("Please provide a Servername and Channel for the Link");
			this.logger.info("------------------------------------------------------------------");
		}

		// if (configReader.getPrensencePlayercount() == true) {
		System.out.println("[DiscordBot] Discord Player Timer Has Started");
		Bukkit.getServer().broadcastMessage("Discord Player Timer Has Started");
		taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (discord.Discord().jda.getStatus() != JDA.Status.CONNECTED) {
					discord.Discord().jda.getPresence().setGame(Game.playing(
							serverName + " With " + Bukkit.getServer().getOnlinePlayers().length + " Players"));
				}
			}
		}, 0L, 2400);
		// }

	}

	public void onDisable() {
		discord.Discord().DiscordSendToChannel(gameBridge, "**SERVER HAS STOPPED** :no:");
		this.logger.info("[DiscordBot] Successfully stopped!");
		// if (configReader.getPrensencePlayercount() == true) {
		Bukkit.getServer().getScheduler().cancelTask(taskId);
		// }
	}

	@Override
	public void execute(Listener listener, Event event) {
		if (botEnabled) {
			if (event instanceof PlayerChatEvent) {
				if (!((PlayerChatEvent) event).getMessage().contains("@")) {
					String message = gameChatMessage;
					message = message.replaceAll("%messageAuthor%", ((PlayerChatEvent) event).getPlayer().getName());
					message = message.replaceAll("%message%", ((PlayerChatEvent) event).getMessage());
					discord.Discord().DiscordSendToChannel(gameBridge, message);
				}

			} else if (event instanceof PlayerJoinEvent) {
				String message = joinMessage;
				message = message.replaceAll("%username%", ((PlayerJoinEvent) event).getPlayer().getName());
				message = message.replaceAll("%onlineCount%",
						Integer.toString(Bukkit.getServer().getOnlinePlayers().length));
				message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
				discord.Discord().DiscordSendToChannel(gameBridge, message);

			} else if (event instanceof PlayerQuitEvent) {
				String message = quitMessage;
				message = message.replaceAll("%username%", ((PlayerQuitEvent) event).getPlayer().getName());
				message = message.replaceAll("%onlineCount%",
						Integer.toString(Bukkit.getServer().getOnlinePlayers().length - 1));
				message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
				discord.Discord().DiscordSendToChannel(gameBridge, message);

			}

		}
	}

	@Override
	public void onEvent(net.dv8tion.jda.core.events.Event event) {
		// TODO Auto-generated method stub
		if (event instanceof GuildMessageReceivedEvent) {
			if (((GuildMessageReceivedEvent) event).getAuthor().isBot()
					|| ((GuildMessageReceivedEvent) event).getAuthor().isFake())
				return;
			TextChannel textChannel = discord.Discord().jda.getTextChannelById(gameBridge);
			String[] messageCMD = ((GuildMessageReceivedEvent) event).getMessage().getContentRaw().split(" ");
			if (((GenericGuildMessageEvent) event).getChannel() == textChannel) {
				if (!((GuildMessageReceivedEvent) event).getMessage().getContentRaw().isEmpty()) {
					String message = discordChatMessage;
					if(((GuildMessageReceivedEvent) event).getMember().getNickname() != null) {
						message = message.replaceAll("%messageAuthor%",
								((GuildMessageReceivedEvent) event).getMember().getNickname());
					} else {
						message = message.replaceAll("%messageAuthor%",
								((GuildMessageReceivedEvent) event).getAuthor().getName());
					}
					message = message.replaceAll("%message%",
							((GuildMessageReceivedEvent) event).getMessage().getContentRaw());
					message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
					Bukkit.getServer().broadcastMessage(message);
				}

			} else if ((messageCMD[0].equalsIgnoreCase("!online")) && (configReader.getOnlineCommand())) {
				String onlineMessage = "**The online players are:** ";
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					onlineMessage += p.getName() + ", ";
				}
				EmbedBuilder eb = new EmbedBuilder();
				eb.setTitle(serverName + " Online Players", null);
				if (Bukkit.getServer().getOnlinePlayers().length > 0) {
					int rnd = new Random().nextInt(Bukkit.getServer().getOnlinePlayers().length);
					Player player = Bukkit.getServer().getOnlinePlayers()[rnd];
					eb.setThumbnail("http://minotar.net/helm/" + player.getName() + "/100.png");
				}
				eb.setColor(Color.red);
				eb.setDescription("There are currently **" + Bukkit.getServer().getOnlinePlayers().length
						+ "** players online\n" + onlineMessage);
				eb.setFooter("https://github.com/RhysB/Discord-Bot-Chatbridge",
						"https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png");

				((GenericGuildMessageEvent) event).getChannel().sendMessage(eb.build()).queue();

			}
		}

	}
	
	public boolean onCommand(CommandSender cs, Command cmdObj, String label, String[] args) {
		String cmd = cmdObj.getName();
		if (cmd.equalsIgnoreCase("discordchatreload")) {
			if (cs.hasPermission("retro.discordchatreload")) {
				this.configReader = new ConfigReader(this);
				gameBridge = this.configReader.getChannel();
				serverName = this.configReader.getServerName();
				// Custom Messages
				discordChatMessage = this.configReader.getDiscordChatMessage();
				gameChatMessage = this.configReader.getGameChatMessage();
				joinMessage = this.configReader.getJoinMessage();
				quitMessage = this.configReader.getQuitMessage();
				cs.sendMessage("Discord Chat Bridge has been reloaded, please do restart tho when you are done testing");
				return true;
			} else {
				cs.sendMessage(ChatColor.RED + "GG mate, no perms tho :(");
				return true;
			}
		} else {
			return false;
		}
	}
}