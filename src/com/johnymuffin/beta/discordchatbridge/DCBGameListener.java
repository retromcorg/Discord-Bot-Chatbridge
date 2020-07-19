package com.johnymuffin.beta.discordchatbridge;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.regex.Pattern;

public class DCBGameListener extends PlayerListener {
    private DiscordChatBridge plugin;

    public DCBGameListener(DiscordChatBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        String chatMessage = plugin.getConfig().getConfigString("message.join-message");
        chatMessage.replace("%username%", event.getPlayer().getName());
        chatMessage.replace("%onlineCount%", String.valueOf(Bukkit.getServer().getOnlinePlayers().length));
        chatMessage.replace("%maxCount%", String.valueOf(Bukkit.getServer().getMaxPlayers()));
        plugin.getDiscordCore().getDiscordBot().discordSendToChannel(plugin.getConfig().getConfigString("channel-id"), chatMessage);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String chatMessage = plugin.getConfig().getConfigString("message.quit-message");
        chatMessage.replace("%username%", event.getPlayer().getName());
        chatMessage.replace("%onlineCount%", String.valueOf(Bukkit.getServer().getOnlinePlayers().length - 1));
        chatMessage.replace("%maxCount%", String.valueOf(Bukkit.getServer().getMaxPlayers()));
        plugin.getDiscordCore().getDiscordBot().discordSendToChannel(plugin.getConfig().getConfigString("channel-id"), chatMessage);
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String chatMessage = plugin.getConfig().getConfigString("message.game-chat-message");
        chatMessage = chatMessage.replace("%messageAuthor%", event.getPlayer().getName());
        chatMessage = chatMessage.replace("%message%", event.getMessage());
        chatMessage = chatMessage.replaceAll(Pattern.quote("@"), " ");
        chatMessage = chatMessage.replaceAll(Pattern.quote("@everyone"), " ");
        chatMessage = chatMessage.replaceAll(Pattern.quote("@here"), " ");
        plugin.getDiscordCore().getDiscordBot().discordSendToChannel(plugin.getConfig().getConfigString("channel-id"), chatMessage);
    }

}
