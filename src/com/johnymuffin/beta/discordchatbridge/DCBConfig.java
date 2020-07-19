package com.johnymuffin.beta.discordchatbridge;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import java.io.File;

public class DCBConfig extends Configuration {


    public DCBConfig(Plugin plugin) {
        super(new File(plugin.getDataFolder(), "config.yml"));
        this.reload();
    }

    private void write() {
        //Main
        generateConfigOption("config-version", 1);
        //Setting
        generateConfigOption("server-name", "BetaServer");
        generateConfigOption("channel-id", "id");
        generateConfigOption("presence-player-count", true);
        generateConfigOption("online-command-enabled", true);
        generateConfigOption("message.discord-chat-message", "&f[&6Discord&f]&7 %messageAuthor%: %message%");
        generateConfigOption("message.game-chat-message", "**%messageAuthor%**: %message%");
        generateConfigOption("message.join-message", "**%username%** Has Joined The Game [%onlineCount%/%maxCount%]");
        generateConfigOption("message.quit-message", "**%username%** Has Quit The Game [%onlineCount%/%maxCount%]");


    }

    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End


    private void reload() {
        this.load();
        this.write();
        this.save();
    }
}
