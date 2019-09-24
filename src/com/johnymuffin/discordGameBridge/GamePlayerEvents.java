package com.johnymuffin.discordGameBridge;

import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Pattern;

public class GamePlayerEvents extends PlayerListener {
    private DiscordBot plugin;
    private DiscordCore dbc;
    private ConfigReader config;

    public GamePlayerEvents(DiscordBot instance, DiscordCore plugin, ConfigReader configReader) {

        this.plugin = instance;
        this.dbc = plugin;
        this.config = configReader;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        String message = config.getJoinMessage();
        message = message.replaceAll("%username%", event.getPlayer().getName());
        message = message.replaceAll("%onlineCount%",
                Integer.toString(Bukkit.getServer().getOnlinePlayers().length));
        message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
        dbc.Discord().DiscordSendToChannel(config.getChannel(), message);

    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String message = config.getQuitMessage();
        message = message.replaceAll("%username%", event.getPlayer().getName());
        message = message.replaceAll("%onlineCount%",
                Integer.toString(Bukkit.getServer().getOnlinePlayers().length - 1));
        message = message.replaceAll("%maxCount%", Integer.toString(Bukkit.getServer().getMaxPlayers()));
        dbc.Discord().DiscordSendToChannel(config.getChannel(), message);
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        String message = config.getGameChatMessage();
        if(!event.isCancelled()) {
            message = message.replaceAll("%messageAuthor%", event.getPlayer().getName());
            message = message.replaceAll("%message%", event.getMessage());
            message = message.replaceAll(Pattern.quote("@"), " ");
            message = message.replaceAll(Pattern.quote("@everyone"), " ");
            message = message.replaceAll(Pattern.quote("@here"), " ");
            dbc.Discord().DiscordSendToChannel(config.getChannel(), message);
        }
    }


}
