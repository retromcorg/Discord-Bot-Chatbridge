package com.johnymuffin.beta.discordchatbridge;

import com.johnymuffin.discordcore.DiscordCore;
import com.johnymuffin.discordcore.DiscordShutdownEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordChatBridge extends JavaPlugin {
    //Basic Plugin Info
    private static DiscordChatBridge plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private DiscordCore discordCore;
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

        if (!Bukkit.getServer().getPluginManager().isPluginEnabled("DiscordCore")) {
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
        discordCore = (DiscordCore) Bukkit.getServer().getPluginManager().getPlugin("DiscordCore");
        //Discord Listener
        discordListener = new DCBDiscordListener(plugin);
        discordCore.getDiscordBot().jda.addEventListener(discordListener);
        //Discord Game Bridge
        final DCBGameListener gameListener = new DCBGameListener(plugin);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, gameListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, gameListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, gameListener, Event.Priority.Highest, this);
        final ShutdownListener shutdownListener = new ShutdownListener();
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, shutdownListener, Event.Priority.Normal, plugin);

        enabled = true;

        //Use runnable to ensure message is posted once server is started
        if (dcbConfig.getConfigBoolean("system.starting-message.enable")) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                String message = dcbConfig.getConfigString("system.starting-message.message");
                message = message.replace("{servername}", getaConfig().getConfigString("server-name"));
                getDiscordCore().getDiscordBot().discordSendToChannel(dcbConfig.getConfigString("channel-id"), message);
            }, 0L);
        }

        if (dcbConfig.getConfigBoolean("presence-player-count")) {
            taskID = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                if (getDiscordCore().getDiscordBot().jda.getStatus() == JDA.Status.CONNECTED) {
                    getDiscordCore().getDiscordBot().jda.getPresence().setActivity(Activity.playing(plugin.getaConfig().getConfigString("server-name") + " With " + Bukkit.getServer().getOnlinePlayers().length + " Players"));
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


            discordCore.getDiscordBot().jda.removeEventListener(discordListener);
            Bukkit.getServer().getScheduler().cancelTask(taskID);
        }
        logger(Level.INFO, "Has been disabled.");
    }


    public void logger(Level level, String message) {
        log.log(level, "[" + pluginName + "] " + message);
    }


    public DCBConfig getaConfig() {
        return dcbConfig;
    }

    public DiscordCore getDiscordCore() {
        return discordCore;
    }

    protected void handleDiscordCoreShutdown() {
        //Discord Shutdown Message
        shutdown = true;
        if (getaConfig().getConfigBoolean("system.shutdown-message.enable")) {
            String message = getaConfig().getConfigString("system.shutdown-message.message");
            message = message.replace("{servername}", getaConfig().getConfigString("server-name"));
            TextChannel textChannel = this.discordCore.getDiscordBot().jda.getTextChannelById(plugin.getaConfig().getConfigString("channel-id"));
            textChannel.sendMessage(message).complete();
        }
    }


    private class ShutdownListener extends CustomEventListener {

        @Override
        public void onCustomEvent(Event event) {
            if (!(event instanceof DiscordShutdownEvent)) {
                return;
            }
            if (shutdown) {
                return;
            }
            handleDiscordCoreShutdown();
            //Force disable this plugin first as it requires DiscordCore to function.
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);

        }

    }
}
