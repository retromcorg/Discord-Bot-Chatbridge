package com.johnymuffin.beta.discordchatbridge;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import com.johnymuffin.jperms.beta.JohnyPerms;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.logging.Level;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class DCBDiscordListener extends ListenerAdapter {
    private DiscordChatBridge plugin;
    private Essentials essentials;
    private File discordCoreDataFolder;
    private File poseidonDataFolder;
    private Map<String, UUID> discordToUUIDCache = new HashMap<>();
    private Map<UUID, String> uuidToUsernameCache = new HashMap<>();

    public DCBDiscordListener(DiscordChatBridge plugin) {
        this.plugin = plugin;
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        this.discordCoreDataFolder = new File(plugin.getDataFolder().getParentFile(), "DiscordCore");
        this.poseidonDataFolder = new File(".");

        if (essentials == null) {
            plugin.logger(Level.WARNING, "Essentials not found! Ignore functionality will not work.");
        } else {
            plugin.logger(Level.INFO, "Essentials found! Ignore functionality enabled.");
        }
        loadPoseidonUUIDCache();
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
            String authenticatedUsername = null;

            if (plugin.getConfig().getConfigBoolean("authentication.enabled")) {
                DiscordAuthentication authPlugin = (DiscordAuthentication) Bukkit.getServer().getPluginManager().getPlugin("DiscordAuthentication");

                //Get playerUUID from DiscordID if possible
                if(authPlugin.getData().isDiscordIDAlreadyLinked(event.getAuthor().getId())) {
                    playerUUID = UUID.fromString(authPlugin.getData().getUUIDFromDiscordID(event.getAuthor().getId()));
                    authenticatedUsername = authPlugin.getData().getLastUsernameFromDiscordID(event.getAuthor().getId());
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

            if (authenticatedUsername == null) {
                Map<String, String> authData = readDiscordAuthenticationConfig(event.getAuthor().getId());
                if (authData != null) {
                    authenticatedUsername = authData.get("username");
                    try {
                        playerUUID = UUID.fromString(authData.get("uuid"));
                    } catch (Exception e) {
                        plugin.logger(Level.WARNING, "Failed to parse UUID from auth config: " + e.getMessage());
                    }
                }
            }

            if (authenticatedUsername == null) {
                UUID discordCoreUUID = getAuthenticatedMinecraftUUID(event.getAuthor().getId());
                if (discordCoreUUID != null) {
                    playerUUID = discordCoreUUID;
                    authenticatedUsername = getUsernameFromUUID(discordCoreUUID);
                }
            }

            if (displayName == null) {
                if (event.getMember().getNickname() != null) {
                    displayName = event.getMember().getNickname();
                } else {
                    displayName = event.getAuthor().getName();
                }
            }

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

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (shouldSendMessage(player, authenticatedUsername)) {
                    player.sendMessage(chatMessage);
                }
            }

            return;
        }
    }

    private boolean shouldSendMessage(Player recipient, String senderUsername) {
        if (essentials == null) {
            return true;
        }
        if (senderUsername == null) {
            return true;
        }
        try {
            User recipientUser = essentials.getUser(recipient);
            if (recipientUser == null) {
                return true;
            }
            boolean isIgnored = recipientUser.isIgnoredPlayer(senderUsername);
            if (isIgnored) {
                return false;
            }
            return true;
        } catch (Exception e) {
            plugin.logger(Level.WARNING, "Error checking ignore status for " + senderUsername + ": " + e.getMessage());
            return true;
        }
    }

    private UUID getAuthenticatedMinecraftUUID(String discordUserId) {
        if (discordToUUIDCache.containsKey(discordUserId)) {
            return discordToUUIDCache.get(discordUserId);
        }

        File authFile = new File(discordCoreDataFolder, "links/" + discordUserId + ".json");
        if (!authFile.exists()) {
            authFile = new File(discordCoreDataFolder, "authenticated/" + discordUserId + ".json");
        }
        if (!authFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(authFile)) {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(reader).getAsJsonObject();

            UUID uuid = null;
            String username = null;

            if (json.has("uuid")) {
                uuid = UUID.fromString(json.get("uuid").getAsString());
            } else if (json.has("minecraftUUID")) {
                uuid = UUID.fromString(json.get("minecraftUUID").getAsString());
            }

            if (json.has("username")) {
                username = json.get("username").getAsString();
            } else if (json.has("minecraftUsername")) {
                username = json.get("minecraftUsername").getAsString();
            } else if (json.has("lastKnownName")) {
                username = json.get("lastKnownName").getAsString();
            } else if (json.has("name")) {
                username = json.get("name").getAsString();
            }

            if (uuid != null) {
                discordToUUIDCache.put(discordUserId, uuid);
                if (username != null) {
                    uuidToUsernameCache.put(uuid, username);
                }
                return uuid;
            }

        } catch (Exception e) {
            plugin.logger(Level.WARNING, "Error reading/parsing auth file for Discord user " + discordUserId + ": " + e.getMessage());
        }
        return null;
    }

    // Might not be the best way to do this, but I always struggle hard with using UUID utils in Poseidon
    private void loadPoseidonUUIDCache() {
        File uuidCacheFile = new File(poseidonDataFolder, "uuidcache.json");
        if (!uuidCacheFile.exists()) {
            plugin.logger(Level.WARNING, "Poseidon uuidcache.json not found at: " + uuidCacheFile.getAbsolutePath());
            plugin.logger(Level.INFO, "Ignore functionality will still work if users are authenticated, but username lookups may be limited.");
            return;
        }

        try (FileReader reader = new FileReader(uuidCacheFile)) {
            JsonParser parser = new JsonParser();
            JsonArray uuidCacheArray = parser.parse(reader).getAsJsonArray();

            for (JsonElement element : uuidCacheArray) {
                JsonObject entry = element.getAsJsonObject();
                if (entry.has("name") && entry.has("uuid")) {
                    String username = entry.get("name").getAsString();
                    String uuidString = entry.get("uuid").getAsString();
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        uuidToUsernameCache.put(uuid, username);
                    } catch (IllegalArgumentException e) {
                        plugin.logger(Level.WARNING, "Invalid UUID in Poseidon cache: " + uuidString);
                    }
                }
            }

            plugin.logger(Level.INFO, "Loaded " + uuidToUsernameCache.size() + " UUID to username mappings from Poseidon cache");

        } catch (Exception e) {
            plugin.logger(Level.WARNING, "Error reading/parsing Poseidon uuidcache.json: " + e.getMessage());
        }
    }

    private String getUsernameFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        String cached = uuidToUsernameCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        if (essentials != null) {
            try {
                User user = essentials.getUser(uuid);
                if (user != null) {
                    String name = user.getName();
                    if (name != null) {
                        uuidToUsernameCache.put(uuid, name);
                        return name;
                    }
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    public void clearCache() {
        discordToUUIDCache.clear();
        uuidToUsernameCache.clear();
        loadPoseidonUUIDCache();
    }

    private Map<String, String> readDiscordAuthenticationConfig(String discordId) {
        File authConfigFile = new File(plugin.getDataFolder().getParentFile(), "DiscordAuthentication/data.yml");

//        plugin.logger(Level.INFO, "Looking for auth config at: " + authConfigFile.getAbsolutePath());

        if (!authConfigFile.exists()) {
            plugin.logger(Level.WARNING, "DiscordAuthentication data.yml file not found!");
            return null;
        }

        try {
            org.bukkit.util.config.Configuration config = new org.bukkit.util.config.Configuration(authConfigFile);
            config.load();

            Object authSection = config.getProperty("authentication");

            if (authSection instanceof Map) {
                Map<?, ?> authMap = (Map<?, ?>) authSection;
                for (Map.Entry<?, ?> entry : authMap.entrySet()) {
                    String uuidKey = String.valueOf(entry.getKey());

                    if (entry.getValue() instanceof Map) {
                        Map<?, ?> userData = (Map<?, ?>) entry.getValue();
                        String storedDiscordId = String.valueOf(userData.get("discordID"));
                        String username = String.valueOf(userData.get("username"));

                        if (storedDiscordId != null && storedDiscordId.equals(discordId)) {
                            Map<String, String> result = new HashMap<>();
                            result.put("username", username);
                            result.put("uuid", uuidKey);
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.logger(Level.WARNING, "Error reading DiscordAuthentication data.yml: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
