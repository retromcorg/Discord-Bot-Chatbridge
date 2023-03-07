package com.johnymuffin.beta.discordchatbridge;

import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class DCBDiscordListener extends ListenerAdapter {
    private DiscordChatBridge plugin;

    public DCBDiscordListener(DiscordChatBridge plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("online")) {
            MessageBuilder messageBuilder = new MessageBuilder();


            String onlineMessage = "**The online players are:** ";
            if (Bukkit.getServer().getOnlinePlayers().length == 1) {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    onlineMessage += p.getName();
                }
            } else {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    onlineMessage += p.getName() + ", ";
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            if(Bukkit.getServer().getOnlinePlayers().length == 0)
            {
                eb.setTitle(plugin.getConfig().getConfigString("server-name"), null);
                eb.setColor(Color.red);
                eb.setDescription("There are currently no players online.");
                eb.setFooter("https://github.com/RhysB/Discord-Bot-Chatbridge",
                        "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png");
            }
            else {
                eb.setTitle(plugin.getConfig().getConfigString("server-name") + " Online Players", null);
                if (Bukkit.getServer().getOnlinePlayers().length > 0) {
                    int rnd = new Random().nextInt(Bukkit.getServer().getOnlinePlayers().length);
                    Player player = Bukkit.getServer().getOnlinePlayers()[rnd];
                    eb.setThumbnail("http://minotar.net/helm/" + player.getName() + "/100.png");
                }
                eb.setColor(Color.green);
                eb.setDescription("There are currently **" + Bukkit.getServer().getOnlinePlayers().length
                        + "** players online\n" + onlineMessage);
                eb.setFooter("https://github.com/RhysB/Discord-Bot-Chatbridge",
                        "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png");
            }

            messageBuilder.setEmbeds(eb.build());
            event.reply(messageBuilder.build()).setEphemeral(true).queue();
        }
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
        //Is the message in the game bridge channel
        if (event.getChannel().getId().equalsIgnoreCase(gameBridgeChannelID)) {
            String displayName = null;
            String dmsg = event.getMessage().getContentRaw();

            if (plugin.getConfig().getConfigBoolean("authentication.enabled")) {
                DiscordAuthentication authPlugin = (DiscordAuthentication) Bukkit.getServer().getPluginManager().getPlugin("DiscordAuthentication");
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

            if (displayName == null) {
                if (event.getMember().getNickname() != null) {
                    displayName = event.getMember().getNickname();
                } else {
                    displayName = event.getAuthor().getName();
                }
            }
            String chatMessage = plugin.getConfig().getConfigString("message.discord-chat-message");
            chatMessage = chatMessage.replace("%messageAuthor%", displayName);
            chatMessage = chatMessage.replace("%message%", dmsg);
            chatMessage = chatMessage.replaceAll("(&([a-f0-9]))", "\u00A7$2");
            Bukkit.getServer().broadcastMessage(chatMessage);
            return;
        }


    }


}
