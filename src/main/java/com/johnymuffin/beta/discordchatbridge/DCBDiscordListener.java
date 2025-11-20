package com.johnymuffin.beta.discordchatbridge;

import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import com.johnymuffin.jperms.beta.JohnyPerms;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class DCBDiscordListener extends ListenerAdapter {
    private DiscordChatBridge plugin;

    public DCBDiscordListener(DiscordChatBridge plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //Don't respond to bots
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
        //Don't respond to funky messages
        if (event.getMessage().getContentRaw().isEmpty()) {
            return;
        }

        String gameBridgeChannelID = plugin.getConfig().getConfigString("channel-id");
        String[] messageCMD = event.getMessage().getContentRaw().split(" ");

        //sorry for the mess of copy pasting the code into each if statement -Owen2k6
        //Online Command
        if (messageCMD[0].equalsIgnoreCase("!online") && plugin.getConfig().getConfigBoolean("online-command-enabled")) {

            //Check for if its enabled.
            if (plugin.getConfig().getConfigBoolean("bot-command-channel-enabled")) {
                //Does it match?
                if (Objects.equals(plugin.getConfig().getConfigString("bot-command-channel-id"), event.getChannel().getId())) {
                    //begin Online Command Response
                    String onlineMessage = "**The online players are:** ";
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        onlineMessage += p.getName() + ", ";
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(plugin.getConfig().getConfigString("server-name") + " Online Players", null);
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

                    event.getChannel().sendMessage(eb.build()).queue();
                    return;
                }
                if (plugin.getConfig().getConfigString("bot-command-channel-id").isEmpty() || Objects.equals(plugin.getConfig().getConfigString("bot-command-channel-id"), "id")) {
                    Bukkit.getLogger().warning("You appear to have forgotten to add a channel ID. go to the config and add an ID or disable the bot command channel limiter");
                    Bukkit.getLogger().info("Will proceed like the feature is disabled.");
                    //begin Online Command Response
                    String onlineMessage = "**The online players are:** ";
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        onlineMessage += p.getName() + ", ";
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(plugin.getConfig().getConfigString("server-name") + " Online Players", null);
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

                    event.getChannel().sendMessage(eb.build()).queue();
                    return;
                }

            }
            //Check for if it's not enabled
            if (!plugin.getConfig().getConfigBoolean("bot-command-channel-enabled")) {
                //begin Online Command Response
                String onlineMessage = "**The online players are:** ";
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    onlineMessage += p.getName() + ", ";
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(plugin.getConfig().getConfigString("server-name") + " Online Players", null);
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

                event.getChannel().sendMessage(eb.build()).queue();
                return;
            }
        }

        //Is the message in the game bridge channel
        if (event.getChannel().getId().equalsIgnoreCase(gameBridgeChannelID)) {
            String displayName = null;
            String prefix = null;
            UUID playerUUID = null;

            if (plugin.getConfig().getConfigBoolean("authentication.enabled")) {
                DiscordAuthentication authPlugin = (DiscordAuthentication) Bukkit.getServer().getPluginManager().getPlugin("DiscordAuthentication");

                //Get playerUUID from DiscordID if possible
                if(authPlugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                    playerUUID = UUID.fromString(authPlugin.getData().getUUIDFromDiscordID(event.getAuthor().getId()));
                }

                if (plugin.getConfig().getConfigBoolean("authentication.discord.only-allow-linked-users")) {
                    if (!authPlugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                        event.getChannel().sendMessage(plugin.getConfig().getString("message.require-link")).queue();
                        return;
                    }
                }
                if (plugin.getConfig().getConfigBoolean("authentication.discord.use-in-game-names-if-available")) {
                    displayName = authPlugin.getData().getLastUsernameFromDiscordID(event.getAuthor().getId());
                }
            }

            //Check for prefix
            if (this.plugin.getConfig().getConfigBoolean("johnyperms-prefix-support.enabled")) {
                if (playerUUID != null) {
                    if(Bukkit.getPluginManager().isPluginEnabled("JPerms")) {
                        JohnyPerms jperms = (JohnyPerms) Bukkit.getServer().getPluginManager().getPlugin("JPerms");
                        //Attempt to get prefix from JohnyPerms for user then group
                        prefix = jperms.getUser(playerUUID).getPrefix();
                        if(prefix == null) {
                            prefix = jperms.getUser(playerUUID).getGroup().getPrefix();
                        }
                    } else {
                        this.plugin.logger(Level.WARNING, "JohnyPerms prefix support is enabled but the plugin is not installed or enabled.");
                    }
                } else {
                    this.plugin.logger(Level.WARNING, "JohnyPerms prefix support is enabled but the player UUID is null. This is likely due to the DiscordAuthentication plugin not being installed or enabled.");
                }
            }

            //Reimplemented from f0f832
            String dmsg = event.getMessage().getContentDisplay();
            dmsg = dmsg.replaceAll("(&([a-f0-9]))", "\u00A7$2");
            if (!plugin.getConfig().getConfigBoolean("message.allow-chat-colors")) {
                dmsg = ChatColor.stripColor(dmsg);
            }

            if (displayName == null) {
                if (event.getMember().getNickname() != null) {
                    displayName = event.getMember().getNickname();
                } else {
                    displayName = event.getAuthor().getName();
                }
            }

            //Final prefix check
            if (prefix == null) {
                prefix = "";
            }
            prefix = prefix.replaceAll("(&([a-f0-9]))", "\u00A7$2");

            String chatMessage = plugin.getConfig().getConfigString("message.discord-chat-message");
            chatMessage = chatMessage.replace("%messageAuthor%", displayName);
            chatMessage = chatMessage.replace("%message%", dmsg);
            chatMessage = chatMessage.replaceAll("(&([a-f0-9]))", "\u00A7$2");
            chatMessage = chatMessage.replace("%prefix%", prefix);
            Bukkit.getServer().broadcastMessage(chatMessage);
            return;
        }


    }


}
