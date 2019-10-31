package com.johnymuffin.discordGameBridge;

import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.regex.Pattern;

public class GamePlayerEvents extends PlayerListener {
    private PermissionManager pex;
    private DiscordBot plugin;
    private DiscordCore dbc;
    private ConfigReader config;

    public GamePlayerEvents(DiscordBot instance, DiscordCore plugin, ConfigReader configReader) {
        this.plugin = instance;
        this.dbc = plugin;
        this.config = configReader;
        //PluginManager manager = plugin.getServer().getPluginManager();
        pex = PermissionsEx.getPermissionManager();
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

    private String replaceMsg(String trueMsg) {
        trueMsg = trueMsg.replaceAll(Pattern.quote("@"), " ");
        trueMsg = trueMsg.replaceAll(Pattern.quote("@everyone"), " ");
        trueMsg = trueMsg.replaceAll(Pattern.quote("@here"), " ");
        trueMsg = trueMsg.replaceAll(Pattern.quote("*"), "\\*");
        trueMsg = trueMsg.replaceAll(Pattern.quote("_"), "\\_");
        trueMsg = trueMsg.replaceAll(Pattern.quote("|"), "\\|");
        trueMsg = trueMsg.replaceAll(Pattern.quote("~"), "\\~");
        return trueMsg;
    }

    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        if(!event.isCancelled()) {
            String message = config.getGameChatMessage();
            String trueMsg = replaceMsg(event.getMessage());
            String groupName = "MISSINGPEX";
            if(pex != null) {
                String pName = event.getPlayer().getName();
                groupName = pex.getUser(pName).getGroups()[0].getName();
                // [0] is always the top hierarchy group that the player has
            }

            message = message.replaceAll("%group%", groupName);
            message = message.replaceAll("%messageAuthor%", event.getPlayer().getName());
            message = message.replaceAll("%message%", trueMsg);
            dbc.Discord().DiscordSendToChannel(config.getChannel(), message);
        }
    }


}