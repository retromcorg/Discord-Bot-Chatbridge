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

    private String discordChatMessage = "&f[&6Discord&f]&7 %messageAuthor%: %message%";
    private String gameChatMessage = "**%messageAuthor%**: %message%";
    private String joinMessage = "**%username%** Has Joined THe Game [%onlineCount%/%maxCount%]";
    private String quitMessage = "**%username%** Has Quit THe Game [%onlineCount%/%maxCount%]";

    private String serverName = "";
    private String channelID = "";
    private Boolean PrensencePlayercount = true;
    private Boolean OnlineCommand = true;
    private boolean announceStartStop = true;

    ConfigReader(DiscordBot instance) {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(instance.config));
        } catch (IOException var4) {
            instance.logger.warning("[RetroBot] No properties found! Making new file...");
            var4.printStackTrace();
        }
        // New Config
        serverName = prop.getProperty("serverName", serverName);
        channelID = prop.getProperty("channelID", channelID);
        PrensencePlayercount = Boolean.parseBoolean(prop.getProperty("PrensencePlayercount", "" + PrensencePlayercount));
        OnlineCommand = Boolean.parseBoolean(prop.getProperty("OnlineCommand", "" + OnlineCommand));
        announceStartStop = Boolean.parseBoolean(prop.getProperty("announceStartStop", "" + announceStartStop));
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
        //Announce Server Start/Stop

        prop.setProperty("OnlineCommand", "" + OnlineCommand);
        prop.setProperty("PrensencePlayercount", "" + PrensencePlayercount);
        prop.setProperty("channelID", channelID);
        prop.setProperty("serverName", serverName);
        prop.setProperty("announceStartStop", ""+announceStartStop);

        try {
            prop.store(new FileOutputStream(instance.config), "Properties for DiscordBot");
        } catch (Exception var3) {
            instance.logger.severe("Failed to save properties for RetroBot!");
            var3.printStackTrace();
        }
    }

    String getServerName() {
        return this.serverName;
    }

    String getChannel() {
        return this.channelID;
    }

    String getJoinMessage() {
        return this.joinMessage;
    }
    String getQuitMessage() {
        return this.quitMessage;
    }
    String getDiscordChatMessage() {
        return this.discordChatMessage;
    }
    String getGameChatMessage() {
        return this.gameChatMessage;
    }

    Boolean getPrensencePlayercount() {
        return this.PrensencePlayercount;

    }
    Boolean getOnlineCommand() {
        return this.OnlineCommand;
    }

    boolean canAnnounceStartStop() {
        return announceStartStop;
    }
}