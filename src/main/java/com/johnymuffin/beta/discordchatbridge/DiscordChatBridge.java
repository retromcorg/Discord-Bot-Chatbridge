package com.johnymuffin.beta.discordchatbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.retromc.discordcore.v6.DiscordCorePlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordChatBridge extends JavaPlugin {
    //Basic Plugin Info
    private static DiscordChatBridge plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private DiscordCorePlugin discordCore;
    private DCBConfig dcbConfig;
    //Other plugin stuff
    private DCBDiscordListener discordListener; //Discord Listener
    private boolean enabled = false;
    private Integer taskID = null;
    private boolean shutdown = false;

    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());

        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordCore-6")) {
            log.info("}---------------ERROR---------------{");
            log.info("Discord Chat Bridge Requires Discord Core");
            log.info("Download it at: https://github.com/RhysB/Discord-Bot-Core");
            log.info("}---------------ERROR---------------{");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        dcbConfig = new DCBConfig(plugin);
        if (dcbConfig.getConfigString("channel-id").isEmpty() || dcbConfig.getConfigString("channel-id").equalsIgnoreCase("id")) {
            log.info("}----------------------------ERROR----------------------------{");
            this.log.info("Please provide a Servername and Channel for the Link");
            log.info("}----------------------------ERROR----------------------------{");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        if (dcbConfig.getConfigBoolean("webhook.use-webhook")) {
            if (dcbConfig.getConfigString("webhook.url") == null || dcbConfig.getConfigString("webhook.url").isEmpty() || dcbConfig.getConfigString("webhook.url").equalsIgnoreCase("url")) {
                log.info("}----------------------------ERROR----------------------------{");
                this.log.info("Please provide a valid Discord Webhook url");
                log.info("}----------------------------ERROR----------------------------{");
                Bukkit.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }
        }

        if (dcbConfig.getConfigBoolean("authentication.enabled") && !Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordAuthentication")) {
            log.info("}---------------ERROR---------------{");
            log.info("Authentication support is enabled, however, the plugin isn't installed.");
            log.info("Download it at: https://github.com/RhysB/Discord-Bot-Core");
            log.info("}---------------ERROR---------------{");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        //Discord Core
        discordCore = (DiscordCorePlugin) Bukkit.getServer().getPluginManager().getPlugin("DiscordCore-6");
        //Discord Listener
        discordListener = new DCBDiscordListener(plugin);
        discordCore.getDiscordBot().getJDA().addEventListener(discordListener);
        //Discord Game Bridge
        final DCBGameListener gameListener = new DCBGameListener(plugin);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, gameListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, gameListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, gameListener, Event.Priority.Highest, this);

        enabled = true;

        //Use runnable to ensure message is posted once server is started
        if (dcbConfig.getConfigBoolean("system.starting-message.enable")) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                String message = dcbConfig.getConfigString("system.starting-message.message");
                message = message.replace("{servername}", getConfig().getConfigString("server-name"));
                getDiscordCore().getDiscordBot().sendMessageToChannel(dcbConfig.getConfigString("channel-id"), message);
            }, 0L);
        }

        if (dcbConfig.getConfigBoolean("presence-player-count")) {
            taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                final JDA jda = getDiscordCore().getDiscordBot().getJDA();
                if (jda != null && jda.getStatus() == JDA.Status.CONNECTED) {
                    String bktString = dcbConfig.getConfigString("presence-message");
                    bktString = bktString.replace("{servername}", plugin.getConfig().getConfigString("server-name"));
                    bktString = bktString.replace("{onlineCount}", String.valueOf(Bukkit.getServer().getOnlinePlayers().length));
                    bktString = bktString.replace("{maxOnlineCount}", String.valueOf(Bukkit.getServer().getMaxPlayers()));
                    jda.getPresence().setActivity(Activity.playing(bktString));
                }


            }, 0L, 20 * 60);
        }

    }

    @Override
    public void onDisable() {
        if (enabled) {
            logger(Level.INFO, "Disabling.");
            if (!shutdown) {
                handleDiscordCoreShutdown();
            }


            final JDA jda = getUsableJDA();
            if (jda != null) {
                jda.removeEventListener(discordListener);
            }
            if (taskID != null) {
                Bukkit.getServer().getScheduler().cancelTask(taskID);
                taskID = null;
            }
        }
        logger(Level.INFO, "Has been disabled.");
    }


    public void logger(Level level, String message) {
        log.log(level, "[" + pluginName + "] " + message);
    }


    public DCBConfig getConfig() {
        return dcbConfig;
    }

    public DiscordCorePlugin getDiscordCore() {
        return discordCore;
    }

    private JDA getUsableJDA() {
        if (discordCore == null) {
            return null;
        }
        final JDA jda = discordCore.getDiscordBot().getJDA();
        if (jda == null) {
            return null;
        }
        final JDA.Status status = jda.getStatus();
        if (status == JDA.Status.SHUTTING_DOWN || status == JDA.Status.SHUTDOWN) {
            return null;
        }
        return jda;
    }

    protected void handleDiscordCoreShutdown() {
        //Discord Shutdown Message
        shutdown = true;
        if (getConfig().getConfigBoolean("system.shutdown-message.enable")) {
            final JDA jda = getUsableJDA();
            if (jda == null) {
                logger(Level.WARNING, "Unable to send the shutdown message as the Discord bot is already offline.");
                return;
            }
            String message = getConfig().getConfigString("system.shutdown-message.message");
            message = message.replace("{servername}", getConfig().getConfigString("server-name"));
            TextChannel textChannel = jda.getTextChannelById(plugin.getConfig().getConfigString("channel-id"));
            if (textChannel == null) {
                logger(Level.WARNING, "Unable to send the shutdown message as the channel-id in the config couldn't be found.");
                return;
            }
            textChannel.sendMessage(message).complete();
        }
    }
}
