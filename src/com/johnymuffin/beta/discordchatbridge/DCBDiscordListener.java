package com.johnymuffin.beta.discordchatbridge;

import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

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

        String gameBridgeChannelID = plugin.getaConfig().getConfigString("channel-id");
        String[] messageCMD = event.getMessage().getContentRaw().split(" ");

        //sorry for the mess of copy pasting the code into each if statement -Owen2k6
        //Online Command
        if (messageCMD[0].equalsIgnoreCase("!online") && plugin.getaConfig().getConfigBoolean("online-command-enabled")) {

            //Check for if its enabled.
            if (plugin.getaConfig().getConfigBoolean("bot-command-channel-enabled")) {
                //Does it match?
                if (Objects.equals(plugin.getaConfig().getConfigString("bot-command-channel-id"), event.getChannel().getId())) {
                    //begin Online Command Response
                    String onlineMessage = "**The online players are:** ";
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        onlineMessage += p.getName() + ", ";
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(plugin.getaConfig().getConfigString("server-name") + " Online Players", null);
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
                if (plugin.getaConfig().getConfigString("bot-command-channel-id").isEmpty() || Objects.equals(plugin.getaConfig().getConfigString("bot-command-channel-id"), "id")) {
                    Bukkit.getLogger().warning("You appear to have forgotten to add a channel ID. go to the config and add an ID or disable the bot command channel limiter");
                    Bukkit.getLogger().info("Will proceed like the feature is disabled.");
                    //begin Online Command Response
                    String onlineMessage = "**The online players are:** ";
                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                        onlineMessage += p.getName() + ", ";
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(plugin.getaConfig().getConfigString("server-name") + " Online Players", null);
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
            if (!plugin.getaConfig().getConfigBoolean("bot-command-channel-enabled")) {
                //begin Online Command Response
                String onlineMessage = "**The online players are:** ";
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    onlineMessage += p.getName() + ", ";
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(plugin.getaConfig().getConfigString("server-name") + " Online Players", null);
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

            if (plugin.getaConfig().getConfigBoolean("authentication.enabled")) {
                DiscordAuthentication authPlugin = (DiscordAuthentication) Bukkit.getServer().getPluginManager().getPlugin("DiscordAuthentication");
                if (plugin.getaConfig().getConfigBoolean("authentication.discord.only-allow-linked-users")) {
                    if (!authPlugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                        event.getChannel().sendMessage(plugin.getConfig().getString("message.require-link")).queue();
                        return;
                    }
                }
                if (plugin.getaConfig().getConfigBoolean("authentication.discord.use-in-game-names-if-available")) {
                    displayName = authPlugin.getData().getLastUsernameFromDiscordID(event.getAuthor().getId());
                }

            }

            if (displayName == null) {
                if (event.getMember().getNickname() != null) {
                    displayName = event.getMember().getNickname();
                } else {
                    displayName = event.getAuthor().getName();
                }
            }

            String chatMessage = plugin.getaConfig().getConfigString("message.discord-chat-message");
            chatMessage = chatMessage.replace("%messageAuthor%", displayName);
            chatMessage = chatMessage.replace("%message%", event.getMessage().getContentDisplay());
            chatMessage = chatMessage.replaceAll("(&([a-f0-9]))", "\u00A7$2");
            Bukkit.getServer().broadcastMessage(chatMessage);
            return;
        }


    }


}
