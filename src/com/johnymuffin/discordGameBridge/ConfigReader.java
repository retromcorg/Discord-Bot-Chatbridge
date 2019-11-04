package com.johnymuffin.discordGameBridge;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class ConfigReader {
    private String discordStartMessage = "**Server Has Started**";
    private String discordChatMessage = "&f[&6Discord&f]&7 %messageAuthor%: %message%";
    private String gameChatMessage = "**%messageAuthor%**: %message%";
    private String joinMessage = "**%username%** Has Joined The Game [%onlineCount%/%maxCount%]";
    private String quitMessage = "**%username%** Has Quit The Game [%onlineCount%/%maxCount%]";
    private String timestampMessage = "**[%world%( - )%h%:%m%:%s%:%S% %ampm% %z%]** ";
    private String serverName = "";
    private String channelID = "";

    private boolean presencePlayercount = true;
    private boolean onlineCommand = true;
    private boolean announceStartStop = true;
    private boolean showTimestamp = true;
    private boolean useInGameTime = true;
    private boolean seperateChat = true;

    ConfigReader() {
        this.reload();
    }

    private int reloadTimes = 0;
    void reload() {
        Properties prop = new Properties();

        DiscordBot instance = DiscordBot.getInstance();
        if(reloadTimes > 3) {
            reloadTimes = 0;
            instance.logger.severe("[DiscordBot]: Failed to create/read properties file after 3 times at " + instance.config.getAbsolutePath());
            return;
        }

        try {
            prop.load(new FileInputStream(instance.config));
        } catch (IOException var4) {
            instance.logger.warning("[DiscordBot]: No properties file found! Attempting to create...");
            if(!instance.config.exists()) {
                try {
                    boolean b = instance.config.createNewFile();
                    if(!b) {
                        instance.logger.warning("[DiscordBot]: Failed to create file at " + instance.config.getAbsolutePath());
                    } else {
                        reloadTimes++;
                        instance.logger.fine("[DiscordBot]: Successfully created new config, trying again...");
                        reload();
                    }
                    return;
                } catch(IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                if(!instance.config.canRead()) {
                    instance.logger.severe("[DiscordBot]: Can't read from config file at " + instance.config.getAbsolutePath());
                    return;
                }
            }
        }

        reloadTimes = 0;

        seperateChat = getAndSet_B(prop, "separateChat", seperateChat);
        showTimestamp = getAndSet_B(prop, "showTimestamp", showTimestamp);
        useInGameTime = getAndSet_B(prop, "useInGameTime", useInGameTime);
        presencePlayercount = getAndSet_B(prop, "presencePlayercount", presencePlayercount);
        onlineCommand = getAndSet_B(prop, "onlineCommand", onlineCommand);
        announceStartStop = getAndSet_B(prop, "announceStartStop", announceStartStop);

        serverName = getAndSet_S(prop, "serverName", serverName);
        channelID = getAndSet_S(prop, "channelID", channelID);
        joinMessage = getAndSet_S(prop, "joinMessage", joinMessage);
        quitMessage = getAndSet_S(prop, "quitMessage", quitMessage);
        discordStartMessage = getAndSet_S(prop, "discordStartMessage", discordStartMessage);
        discordChatMessage = getAndSet_S(prop, "discordChatMessage", discordChatMessage);
        gameChatMessage = getAndSet_S(prop, "gameChatMessage", gameChatMessage);
        timestampMessage = getAndSet_S(prop, "timestampMessage", timestampMessage);

        try {
            prop.store(new FileOutputStream(instance.config), "Properties for DiscordBot");
        } catch (Exception e) {
            instance.logger.severe("[DiscordBot]: Failed to save properties!");
            e.printStackTrace();
        }
    }

    private String getAndSet_S(@Nonnull Properties prop, @Nonnull String get, @Nonnull Object Default) {
        String s = prop.getProperty(get, String.valueOf(Default));
        prop.setProperty(get, String.valueOf(s));
        return s;
    }
    private boolean getAndSet_B(@Nonnull Properties prop, @Nonnull String get, @Nonnull Object Default) {
        Boolean s = Boolean.parseBoolean(prop.getProperty(get, String.valueOf(Default)));
        prop.setProperty(get, String.valueOf(s));
        return s;
    }

    String getServerName() { return this.serverName; }
    String getChannel() { return this.channelID; }
    String getJoinMessage() { return this.joinMessage; }
    String getQuitMessage() { return this.quitMessage; }
    String getDiscordStartMessage() { return discordStartMessage; }
    String getDiscordChatMessage() { return this.discordChatMessage; }
    String getGameChatMessage() { return this.gameChatMessage; }
    String getTimestampMessage() { return timestampMessage; }

    boolean getPresencePlayercount() { return this.presencePlayercount; }
    boolean getOnlineCommand() { return this.onlineCommand; }

    boolean canUseInGameTime() { return useInGameTime; }
    boolean canShowTimestamp() { return showTimestamp; }
    boolean canSeparateChat() { return seperateChat; }
    boolean canAnnounceStartStop() { return announceStartStop; }
}